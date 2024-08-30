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

package com.io7m.azeno.tests.database;

import com.io7m.azeno.database.api.AzAssetGetType;
import com.io7m.azeno.database.api.AzAssetPutType;
import com.io7m.azeno.database.api.AzCollectionAccessGetType;
import com.io7m.azeno.database.api.AzCollectionAccessSetType;
import com.io7m.azeno.database.api.AzCollectionGetType;
import com.io7m.azeno.database.api.AzCollectionPutType;
import com.io7m.azeno.database.api.AzCollectionSearchType;
import com.io7m.azeno.database.api.AzDatabaseConnectionType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.database.api.AzDatabaseType;
import com.io7m.azeno.database.api.AzSchemaPutType;
import com.io7m.azeno.database.api.AzStoreGetType;
import com.io7m.azeno.database.api.AzStorePutType;
import com.io7m.azeno.database.api.AzUserPutType;
import com.io7m.azeno.model.AzAsset;
import com.io7m.azeno.model.AzAssetID;
import com.io7m.azeno.model.AzAssetProperties;
import com.io7m.azeno.model.AzCollection;
import com.io7m.azeno.model.AzCollectionID;
import com.io7m.azeno.model.AzHashSHA256;
import com.io7m.azeno.model.AzSchema;
import com.io7m.azeno.model.AzSchemaID;
import com.io7m.azeno.model.AzStoreID;
import com.io7m.azeno.model.AzStoreS3;
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.model.AzValueBoolean;
import com.io7m.azeno.model.AzValueFloating;
import com.io7m.azeno.model.AzValueInteger;
import com.io7m.azeno.model.AzValueString;
import com.io7m.azeno.model.AzValueTimestamp;
import com.io7m.azeno.model.AzValueURI;
import com.io7m.azeno.model.AzValueUUID;
import com.io7m.azeno.tests.containers.AzDatabaseFixture;
import com.io7m.azeno.tests.containers.AzFixtures;
import com.io7m.ervilla.api.EContainerSupervisorType;
import com.io7m.ervilla.test_extension.ErvillaCloseAfterSuite;
import com.io7m.ervilla.test_extension.ErvillaConfiguration;
import com.io7m.ervilla.test_extension.ErvillaExtension;
import com.io7m.huanuco.api.HClientAccessKeys;
import com.io7m.idstore.model.IdName;
import com.io7m.lanark.core.RDottedName;
import com.io7m.medrina.api.MSubject;
import com.io7m.zelador.test_extension.CloseableResourcesType;
import com.io7m.zelador.test_extension.ZeladorExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigInteger;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({ErvillaExtension.class, ZeladorExtension.class})
@ErvillaConfiguration(projectName = "com.io7m.azeno", disabledIfUnsupported = true)
public final class AzDatabaseAssetsTest
{
  private static AzDatabaseFixture DATABASE_FIXTURE;
  private AzDatabaseConnectionType connection;
  private AzDatabaseTransactionType transaction;
  private AzDatabaseType database;
  private AzStorePutType storePut;
  private AzStoreGetType storeGet;
  private AzUserPutType userPut;
  private AzCollectionPutType collectionPut;
  private AzCollectionGetType collectionGet;
  private AzCollectionSearchType collectionSearch;
  private AzCollectionAccessSetType collectionAccessSet;
  private AzCollectionAccessGetType collectionAccessGet;
  private AzUser user;
  private AzStoreS3 store;
  private AzCollection collection;
  private AzAssetProperties assetProperties;
  private AzAssetPutType assetPut;
  private AzAssetGetType assetGet;
  private AzSchemaPutType schemaPut;
  private AzSchema schema;

  @BeforeAll
  public static void setupOnce(
    final @ErvillaCloseAfterSuite EContainerSupervisorType containers)
    throws Exception
  {
    DATABASE_FIXTURE =
      AzFixtures.database(AzFixtures.pod(containers));
  }

  @BeforeEach
  public void setup(
    final CloseableResourcesType closeables)
    throws Exception
  {
    DATABASE_FIXTURE.reset();

    this.database =
      closeables.addPerTestResource(DATABASE_FIXTURE.createDatabase());
    this.connection =
      closeables.addPerTestResource(this.database.openConnection());
    this.transaction =
      closeables.addPerTestResource(this.connection.openTransaction());

    this.schema =
      new AzSchema(
        new AzSchemaID(new RDottedName("com.io7m.example"), 1),
        Map.of()
      );

    this.schemaPut =
      this.transaction.query(AzSchemaPutType.class);
    this.storePut =
      this.transaction.query(AzStorePutType.class);
    this.storeGet =
      this.transaction.query(AzStoreGetType.class);
    this.userPut =
      this.transaction.query(AzUserPutType.class);
    this.collectionPut =
      this.transaction.query(AzCollectionPutType.class);
    this.collectionGet =
      this.transaction.query(AzCollectionGetType.class);
    this.collectionSearch =
      this.transaction.query(AzCollectionSearchType.class);
    this.collectionAccessSet =
      this.transaction.query(AzCollectionAccessSetType.class);
    this.collectionAccessGet =
      this.transaction.query(AzCollectionAccessGetType.class);
    this.assetPut =
      this.transaction.query(AzAssetPutType.class);
    this.assetGet =
      this.transaction.query(AzAssetGetType.class);

    this.user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    this.store =
      new AzStoreS3(
        AzStoreID.random(),
        "Store 1",
        "us-east-1",
        URI.create("https://s3.example.com"),
        Optional.of(
          new HClientAccessKeys("abcd", "1234")
        )
      );

    this.collection =
      new AzCollection(
        AzCollectionID.random(),
        "Collection 1",
        this.store.id(),
        this.schema.id()
      );

    this.assetProperties =
      AzAssetProperties.builder()
        .put(
          new AzValueBoolean(
            new RDottedName("a.k0"),
            true)
        )
        .put(
          new AzValueFloating(
            new RDottedName("a.k1"),
            Math.PI)
        )
        .put(
          new AzValueInteger(
            new RDottedName("a.k2"),
            BigInteger.valueOf(Long.MAX_VALUE))
        )
        .put(
          new AzValueString(
            new RDottedName("a.k3"),
            "Some text.")
        )
        .put(
          new AzValueTimestamp(
            new RDottedName("a.k4"),
            OffsetDateTime.now(ZoneId.of("UTC"))
          )
        )
        .put(
          new AzValueURI(
            new RDottedName("a.k5"),
            URI.create("https://www.io7m.com/")
          )
        )
        .put(
          new AzValueUUID(
            new RDottedName("a.k6"),
            UUID.randomUUID()
          )
        )
        .build();
  }

  /**
   * Creating assets works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testAssetCreate0()
    throws Exception
  {
    this.transaction.setUserID(this.user.userId());
    this.userPut.execute(this.user);
    this.storePut.execute(this.store);
    this.schemaPut.execute(this.schema);
    this.collectionPut.execute(this.collection);

    final var asset =
      new AzAsset(
        AzAssetID.random(),
        this.collection.id(),
        new AzHashSHA256(
          "5891b5b522d5df086d0ff0b110fbd9d21bb4fc7163af34d08286a2e846f6be03"
        ),
        this.assetProperties
      );

    this.assetPut.execute(asset);
    assertEquals(
      asset,
      this.assetGet.execute(asset.id()).orElseThrow());
  }
}
