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


package com.io7m.azeno.server.service.solr.internal;

import com.io7m.azeno.model.AzSchema;
import com.io7m.azeno.model.AzSchemaFieldBoolean;
import com.io7m.azeno.model.AzSchemaFieldFloating;
import com.io7m.azeno.model.AzSchemaFieldInteger;
import com.io7m.azeno.model.AzSchemaFieldStringLocal;
import com.io7m.azeno.model.AzSchemaFieldStringUninterpreted;
import com.io7m.azeno.model.AzSchemaFieldTextType;
import com.io7m.azeno.model.AzSchemaFieldTimestamp;
import com.io7m.azeno.model.AzSchemaFieldType;
import com.io7m.azeno.model.AzSchemaFieldURI;
import com.io7m.azeno.model.AzSchemaFieldUUID;
import org.apache.solr.client.solrj.request.schema.FieldTypeDefinition;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The standard schemas.
 */

public final class AzSolrSchemas
{
  private static final AzSolrField FIELD_HASH_ALGORITHM =
    new AzSolrField(
      "azeno.hash_algorithm",
      "string",
      true,
      true,
      false
    );

  private static final AzSolrField FIELD_HASH_VALUE =
    new AzSolrField(
      "azeno.hash_value",
      "lowercase",
      true,
      true,
      false
    );

  private static final AzSolrField FIELD_SIZE =
    new AzSolrField(
      "azeno.size",
      "plong",
      true,
      true,
      false
    );

  private static final AzSolrField FIELD_CONTENT_TYPE =
    new AzSolrField(
      "azeno.content_type",
      "string",
      true,
      true,
      false
    );

  private AzSolrSchemas()
  {

  }

  /**
   * Convert the given schema to a list of update commands.
   *
   * @param schema       The schema
   * @param collectionId The collection
   *
   * @return The commands
   */

  public static SchemaRequest.MultiUpdate toCommands(
    final AzSolrSchema schema,
    final String collectionId)
  {
    final var requests =
      new ArrayList<SchemaRequest.Update>(schema.fields().size());

    final var q = new ModifiableSolrParams();
    q.set("collection", collectionId);

    for (final var field : schema.fieldTypes()) {
      final var a =
        Map.<String, Object>ofEntries(
          Map.entry("name", field.name()),
          Map.entry("class", field.className()),
          Map.entry("multiValued", Boolean.toString(field.multiValued()))
        );

      final var t = new FieldTypeDefinition();
      t.setAttributes(a);
      requests.add(new SchemaRequest.AddFieldType(t, q));
    }

    for (final var field : schema.fields()) {
      final var a =
        Map.<String, Object>ofEntries(
          Map.entry("name", field.name()),
          Map.entry("type", field.type()),
          Map.entry("indexed", Boolean.toString(field.indexed())),
          Map.entry("stored", Boolean.toString(field.stored())),
          Map.entry("multiValued", Boolean.toString(field.multiValued()))
        );
      requests.add(new SchemaRequest.AddField(a, q));
    }

    return new SchemaRequest.MultiUpdate(requests, q);
  }

  /**
   * Convert a schema to a Solr schema.
   *
   * @param schema The schema
   *
   * @return The Solr schema
   */

  public static AzSolrSchema toSolrSchema(
    final AzSchema schema)
  {
    final var solrFields =
      new HashMap<String, AzSolrField>();

    for (final var entry : schema.fieldTypes().entrySet()) {
      final var fieldName =
        entry.getKey();
      final var fieldType =
        entry.getValue();

      solrFields.put(
        fieldName.value(),
        solrFieldTypeOf(fieldName.value(), fieldType)
      );
    }

    solrFields.put(FIELD_HASH_ALGORITHM.name(), FIELD_HASH_ALGORITHM);
    solrFields.put(FIELD_HASH_VALUE.name(), FIELD_HASH_VALUE);
    solrFields.put(FIELD_SIZE.name(), FIELD_SIZE);
    solrFields.put(FIELD_CONTENT_TYPE.name(), FIELD_CONTENT_TYPE);

    final var solrFieldTypes =
      new ArrayList<AzSolrFieldType>();

    solrFieldTypes.add(
      new AzSolrFieldType(
        "azeno_uuids",
        "solr.UUIDField",
        true
      )
    );
    solrFieldTypes.add(
      new AzSolrFieldType(
        "azeno_uuid",
        "solr.UUIDField",
        false
      )
    );

    solrFieldTypes.add(
      new AzSolrFieldType(
        "azeno_uris",
        "solr.StrField",
        true
      )
    );
    solrFieldTypes.add(
      new AzSolrFieldType(
        "azeno_uri",
        "solr.StrField",
        false
      )
    );

    return new AzSolrSchema(
      List.copyOf(solrFieldTypes),
      List.copyOf(solrFields.values())
    );
  }

  private static AzSolrField solrFieldTypeOf(
    final String fieldName,
    final AzSchemaFieldType<?> fieldType)
  {
    return switch (fieldType) {
      case final AzSchemaFieldBoolean f -> {
        yield new AzSolrField(
          fieldName,
          f.multiValued() ? "booleans" : "boolean",
          true,
          true,
          f.multiValued()
        );
      }
      case final AzSchemaFieldFloating f -> {
        yield new AzSolrField(
          fieldName,
          f.multiValued() ? "pdoubles" : "pdouble",
          true,
          true,
          f.multiValued()
        );
      }
      case final AzSchemaFieldInteger f -> {
        yield new AzSolrField(
          fieldName,
          f.multiValued() ? "plongs" : "plong",
          true,
          true,
          f.multiValued()
        );
      }
      case final AzSchemaFieldTextType f -> {
        yield solrFieldTextOf(fieldName, f);
      }
      case final AzSchemaFieldTimestamp f -> {
        yield new AzSolrField(
          fieldName,
          f.multiValued() ? "pdates" : "pdate",
          true,
          true,
          f.multiValued()
        );
      }
      case final AzSchemaFieldURI f -> {
        yield new AzSolrField(
          fieldName,
          f.multiValued() ? "azeno_uris" : "azeno_uri",
          true,
          true,
          f.multiValued()
        );
      }
      case final AzSchemaFieldUUID f -> {
        yield new AzSolrField(
          fieldName,
          f.multiValued() ? "azeno_uuids" : "azeno_uuid",
          true,
          true,
          f.multiValued()
        );
      }
    };
  }

  private static AzSolrField solrFieldTextOf(
    final String fieldName,
    final AzSchemaFieldTextType f)
  {
    return switch (f) {
      case final AzSchemaFieldStringLocal ff -> {
        yield new AzSolrField(
          fieldName,
          "text_" + ff.locale().getLanguage(),
          true,
          true,
          f.multiValued()
        );
      }
      case final AzSchemaFieldStringUninterpreted ff -> {
        yield new AzSolrField(
          fieldName,
          f.multiValued() ? "strings" : "string",
          true,
          true,
          f.multiValued()
        );
      }
    };
  }
}
