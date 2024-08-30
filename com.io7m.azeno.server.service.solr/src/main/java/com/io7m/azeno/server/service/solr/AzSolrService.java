/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.azeno.server.service.solr;

import com.io7m.azeno.error_codes.AzException;
import com.io7m.azeno.model.AzAsset;
import com.io7m.azeno.model.AzAssetID;
import com.io7m.azeno.model.AzAssetSummary;
import com.io7m.azeno.model.AzCollection;
import com.io7m.azeno.model.AzCollectionID;
import com.io7m.azeno.model.AzSchema;
import com.io7m.azeno.model.AzValueBoolean;
import com.io7m.azeno.model.AzValueFloating;
import com.io7m.azeno.model.AzValueInteger;
import com.io7m.azeno.model.AzValueString;
import com.io7m.azeno.model.AzValueTimestamp;
import com.io7m.azeno.model.AzValueType;
import com.io7m.azeno.model.AzValueURI;
import com.io7m.azeno.model.AzValueUUID;
import com.io7m.azeno.server.service.solr.internal.AzSolrSchemas;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType;
import com.io7m.azeno.strings.AzStrings;
import io.opentelemetry.context.Context;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;

import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorDuplicate;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorIo;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorResourceCloseFailed;
import static com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType.recordSpanException;
import static com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType.setSpanErrorCode;
import static com.io7m.azeno.strings.AzStringConstants.ERROR_COLLECTION_ALREADY_EXISTS;

/**
 * The Solr service.
 */

public final class AzSolrService
  implements AzSolrServiceType
{
  private final AzServerTelemetryServiceType telemetry;
  private final HttpJdkSolrClient client;
  private final AzStrings strings;

  private AzSolrService(
    final AzServerTelemetryServiceType inTelemetry,
    final HttpJdkSolrClient inClient,
    final AzStrings inStrings)
  {
    this.telemetry =
      Objects.requireNonNull(inTelemetry, "telemetry");
    this.client =
      Objects.requireNonNull(inClient, "client");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
  }

  /**
   * Create a service.
   *
   * @param telemetry The telemetry service
   * @param strings   The string resources
   * @param endpoint  The endpoint (such as "https://search.example.com/solr")
   *
   * @return The service
   */

  public static AzSolrServiceType create(
    final AzServerTelemetryServiceType telemetry,
    final AzStrings strings,
    final URI endpoint)
  {
    final var client =
      new HttpJdkSolrClient.Builder(endpoint.toString())
        .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
        .build();

    return new AzSolrService(telemetry, client, strings);
  }

  @Override
  public String description()
  {
    return "Solr search service";
  }

  @Override
  public synchronized void collectionCreate(
    final AzCollection collection,
    final AzSchema schema)
    throws AzException
  {
    Objects.requireNonNull(collection, "collection");
    Objects.requireNonNull(schema, "schema");

    final var collectionId =
      collection.id().toString();
    final var schemaName =
      schema.id().name().value();
    final var schemaVersion =
      Integer.toUnsignedString(schema.id().version());

    final var attributes = new HashMap<String, String>();
    attributes.put("CollectionID", collectionId);
    attributes.put("Collection Title", collection.title());
    attributes.put("Schema Name", schemaName);
    attributes.put("Schema Version", schemaVersion);

    final var span =
      this.telemetry.tracer()
        .spanBuilder("SolrCollectionCreate")
        .setParent(Context.current())
        .startSpan();

    span.setAttribute("CollectionID", collectionId);
    span.setAttribute("Collection Title", collection.title());
    span.setAttribute("Schema Name", schemaName);
    span.setAttribute("Schema Version", schemaVersion);

    try (final var ignored = span.makeCurrent()) {
      this.collectionCreateInSpan(collectionId, attributes);
      this.collectionCreateSchema(collectionId, attributes, schema);
    } finally {
      span.end();
    }
  }

  private void collectionCreateSchema(
    final String collectionId,
    final HashMap<String, String> attributes,
    final AzSchema schema)
    throws AzException
  {
    final var solrSchema =
      AzSolrSchemas.toSolrSchema(schema);

    try {
      this.client.request(AzSolrSchemas.toCommands(solrSchema, collectionId));
    } catch (final Exception e) {
      recordSpanException(e);
      throw fallbackException(attributes, e);
    }
  }

  private void collectionCreateInSpan(
    final String collectionId,
    final HashMap<String, String> attributes)
    throws AzException
  {
    final var r =
      CollectionAdminRequest.createCollection(
        collectionId,
        1,
        1
      );

    try {
      this.client.request(r);
    } catch (final Exception e) {
      recordSpanException(e);

      if (e.getMessage().contains("collection already exists")) {
        setSpanErrorCode(errorDuplicate());

        throw new AzException(
          this.strings.format(ERROR_COLLECTION_ALREADY_EXISTS),
          e,
          errorDuplicate(),
          attributes,
          Optional.empty()
        );
      }
      throw fallbackException(attributes, e);
    }
  }

  private static AzException fallbackException(
    final Map<String, String> attributes,
    final Exception e)
  {
    return new AzException(
      e.getMessage(),
      e,
      errorIo(),
      attributes,
      Optional.empty()
    );
  }

  @Override
  public synchronized void assetsIndex(
    final List<AzAsset> assets)
    throws AzException
  {
    Objects.requireNonNull(assets, "assets");

    final var span =
      this.telemetry.tracer()
        .spanBuilder("SolrAssetsIndex")
        .setParent(Context.current())
        .startSpan();

    final var collections = new HashSet<AzCollectionID>();
    try (final var ignored = span.makeCurrent()) {
      for (final var asset : assets) {
        final var properties = asset.properties();
        final var document = new SolrInputDocument();
        for (final var entry : properties.values().entrySet()) {
          final var name =
            entry.getKey();
          for (final var value : entry.getValue()) {
            document.addField(name.value(), fieldValueOf(value));
          }
        }

        document.addField("id", asset.id().toString());
        document.addField("azeno.hash_algorithm", asset.hash().name());
        document.addField("azeno.hash_value", asset.hash().value());

        collections.add(asset.collection());
        this.client.add(
          asset.collection().toString(),
          document,
          0
        );
      }

      for (final var collection : collections) {
        this.client.commit(collection.toString(), true, true);
      }
    } catch (final Exception e) {
      recordSpanException(e);
      throw fallbackException(Map.of(), e);
    } finally {
      span.end();
    }
  }

  @Override
  public synchronized AzSolrPage<AzAssetSummary> assetSearchBegin(
    final AzCollectionID collection,
    final String queryText,
    final AzSolrSortField sortField)
    throws AzException
  {
    Objects.requireNonNull(collection, "collection");
    Objects.requireNonNull(queryText, "queryText");
    Objects.requireNonNull(sortField, "sortField");

    final var collectionId =
      collection.id().toString();

    final var attributes = new HashMap<String, String>();
    attributes.put("CollectionID", collectionId);

    final var span =
      this.telemetry.tracer()
        .spanBuilder("SolrAssetSearchBegin")
        .setParent(Context.current())
        .startSpan();

    span.setAttribute("CollectionID", collectionId);

    try (final var ignored = span.makeCurrent()) {
      final SolrQuery query = new SolrQuery(queryText);
      query.addField("id");
      query.add("cursorMark", "*");
      query.setSort(
        sortField.name(),
        sortField.ascending() ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc
      );

      final var response =
        this.client.query(collectionId, query);

      final var results =
        new ArrayList<AzAssetSummary>();

      for (final var document : response.getResults()) {
        results.add(
          new AzAssetSummary(
            AzAssetID.of((String) document.get("id")),
            collection
          )
        );
      }

      return new AzSolrPage<>(
        response.getNextCursorMark(),
        List.copyOf(results)
      );
    } catch (final Exception e) {
      recordSpanException(e);
      throw fallbackException(attributes, e);
    } finally {
      span.end();
    }
  }

  private static String fieldValueOf(
    final AzValueType value)
  {
    return switch (value) {
      case final AzValueBoolean v -> {
        yield Boolean.toString(v.value());
      }
      case final AzValueFloating v -> {
        yield Double.toString(v.value());
      }
      case final AzValueInteger v -> {
        yield v.value().toString();
      }
      case final AzValueString v -> {
        yield v.value();
      }
      case final AzValueTimestamp v -> {
        yield v.value().toString();
      }
      case final AzValueURI v -> {
        yield v.value().toString();
      }
      case final AzValueUUID v -> {
        yield v.value().toString();
      }
    };
  }

  @Override
  public void close()
    throws AzException
  {
    try {
      this.client.close();
    } catch (final IOException e) {
      throw new AzException(
        e.getMessage(),
        e,
        errorResourceCloseFailed(),
        Map.of(),
        Optional.empty()
      );
    }
  }
}
