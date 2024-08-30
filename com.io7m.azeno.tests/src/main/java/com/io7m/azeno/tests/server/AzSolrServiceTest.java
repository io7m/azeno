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


package com.io7m.azeno.tests.server;

import com.io7m.azeno.error_codes.AzException;
import com.io7m.azeno.model.AzAsset;
import com.io7m.azeno.model.AzAssetID;
import com.io7m.azeno.model.AzAssetProperties;
import com.io7m.azeno.model.AzCollection;
import com.io7m.azeno.model.AzCollectionID;
import com.io7m.azeno.model.AzHashSHA256;
import com.io7m.azeno.model.AzSchema;
import com.io7m.azeno.model.AzSchemaFieldBoolean;
import com.io7m.azeno.model.AzSchemaFieldFloating;
import com.io7m.azeno.model.AzSchemaFieldInteger;
import com.io7m.azeno.model.AzSchemaFieldStringLocal;
import com.io7m.azeno.model.AzSchemaFieldStringUninterpreted;
import com.io7m.azeno.model.AzSchemaFieldTimestamp;
import com.io7m.azeno.model.AzSchemaFieldType;
import com.io7m.azeno.model.AzSchemaFieldURI;
import com.io7m.azeno.model.AzSchemaFieldUUID;
import com.io7m.azeno.model.AzSchemaID;
import com.io7m.azeno.model.AzStoreID;
import com.io7m.azeno.model.AzValueInteger;
import com.io7m.azeno.model.AzValueString;
import com.io7m.azeno.server.service.solr.AzSolrService;
import com.io7m.azeno.server.service.solr.AzSolrServiceType;
import com.io7m.azeno.server.service.solr.AzSolrSortField;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryNoOp;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.azeno.tests.containers.AzFixtures;
import com.io7m.azeno.tests.containers.AzSolrFixture;
import com.io7m.ervilla.api.EContainerSupervisorType;
import com.io7m.ervilla.test_extension.ErvillaCloseAfterSuite;
import com.io7m.ervilla.test_extension.ErvillaConfiguration;
import com.io7m.ervilla.test_extension.ErvillaExtension;
import com.io7m.lanark.core.RDottedName;
import com.io7m.zelador.test_extension.CloseableResourcesType;
import com.io7m.zelador.test_extension.ZeladorExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorDuplicate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({ErvillaExtension.class, ZeladorExtension.class})
@ErvillaConfiguration(projectName = "com.io7m.azeno", disabledIfUnsupported = true)
public final class AzSolrServiceTest
{
  private static AzSolrFixture SOLR_FIXTURE;
  private AzSolrServiceType service;
  private AzCollection collection;
  private AzSchema schema1;

  @BeforeAll
  public static void setupOnce(
    final @ErvillaCloseAfterSuite EContainerSupervisorType containers)
    throws Exception
  {
    SOLR_FIXTURE =
      AzFixtures.solr(AzFixtures.pod(containers));
  }

  @BeforeEach
  public void setup(
    final CloseableResourcesType closeables)
    throws Exception
  {
    this.service =
      AzSolrService.create(
        AzServerTelemetryNoOp.noop(),
        AzStrings.create(Locale.ROOT),
        URI.create("http://localhost:%d/solr".formatted(SOLR_FIXTURE.port()))
      );

    this.schema1 =
      new AzSchema(
        new AzSchemaID(
          new RDottedName("com.io7m.example"),
          1
        ),
        Stream.of(
          new AzSchemaFieldBoolean(new RDottedName("a.k0"), true),
          new AzSchemaFieldFloating(new RDottedName("a.k1"), true),
          new AzSchemaFieldInteger(new RDottedName("a.k2"), true),
          new AzSchemaFieldStringLocal(new RDottedName("a.k6"), Locale.ENGLISH, true),
          new AzSchemaFieldStringUninterpreted(new RDottedName("a.k8"), true),
          new AzSchemaFieldTimestamp(new RDottedName("a.k3"), true),
          new AzSchemaFieldURI(new RDottedName("a.k4"), true),
          new AzSchemaFieldUUID(new RDottedName("a.k5"), true)
        ).collect(Collectors.toMap(AzSchemaFieldType::name, f -> f))
      );

    this.collection =
      new AzCollection(
        AzCollectionID.random(),
        "Collection 0",
        AzStoreID.random(),
        this.schema1.id()
      );
  }

  @AfterEach
  public void tearDown()
    throws AzException
  {
    this.service.close();
  }

  /**
   * Creating a collection works.
   *
   * @throws AzException On errors
   */

  @Test
  public void testCreateCollection()
    throws AzException
  {
    this.service.collectionCreate(this.collection, this.schema1);

    final var ex =
      assertThrows(AzException.class, () -> {
        this.service.collectionCreate(this.collection, this.schema1);
      });
    assertEquals(errorDuplicate(), ex.errorCode());
  }

  /**
   * Indexing and searching for assets works.
   *
   * @throws AzException On errors
   */

  @Test
  public void testAssetIndexSearch()
    throws AzException
  {
    this.service.collectionCreate(this.collection, this.schema1);

    final var asset0 =
      new AzAsset(
        AzAssetID.random(),
        this.collection.id(),
        new AzHashSHA256("5891b5b522d5df086d0ff0b110fbd9d21bb4fc7163af34d08286a2e846f6be03"),
        AzAssetProperties.builder()
          .put(new AzValueInteger(new RDottedName("azeno.size"), BigInteger.valueOf(6L)))
          .put(new AzValueString(new RDottedName("azeno.content_type"), "text/plain"))
          .build()
      );

    final var asset1 =
      new AzAsset(
        AzAssetID.random(),
        this.collection.id(),
        new AzHashSHA256("71573b922a87abc3fd1a957f2cfa09d9e16998567dd878a85e12166112751806"),
        AzAssetProperties.builder()
          .put(new AzValueInteger(new RDottedName("azeno.size"), BigInteger.valueOf(7L)))
          .put(new AzValueString(new RDottedName("azeno.content_type"), "text/plain"))
          .build()
      );

    final var asset2 =
      new AzAsset(
        AzAssetID.random(),
        this.collection.id(),
        new AzHashSHA256("efdcc2cfdefcde1c0fd0498fed0e043a284517ddeee93336e1dd1253b37bc2db"),
        AzAssetProperties.builder()
          .put(new AzValueInteger(new RDottedName("azeno.size"), BigInteger.valueOf(5L)))
          .put(new AzValueString(new RDottedName("azeno.content_type"), "image/png"))
          .build()
      );

    this.service.assetsIndex(List.of(asset0, asset1, asset2));

    {
      final var r =
        this.service.assetSearchBegin(
          this.collection.id(),
          "azeno.size:6",
          new AzSolrSortField("id", true)
        );

      assertEquals(1, r.items().size());
      assertEquals(asset0.id(), r.items().get(0).id());
    }

    {
      final var r =
        this.service.assetSearchBegin(
          this.collection.id(),
          "azeno.content_type:image/png",
          new AzSolrSortField("id", true)
        );

      assertEquals(1, r.items().size());
      assertEquals(asset2.id(), r.items().get(0).id());
    }

    {
      final var r =
        this.service.assetSearchBegin(
          this.collection.id(),
          "azeno.size:[6 TO 1000]",
          new AzSolrSortField("id", true)
        );

      assertEquals(2, r.items().size());
    }
  }
}