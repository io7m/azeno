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
import com.io7m.azeno.error_codes.AzStandardErrorCodes;
import com.io7m.azeno.model.AzCollection;
import com.io7m.azeno.model.AzCollectionAccess;
import com.io7m.azeno.model.AzCollectionID;
import com.io7m.azeno.model.AzSchema;
import com.io7m.azeno.model.AzSchemaID;
import com.io7m.azeno.model.AzStoreID;
import com.io7m.azeno.model.AzStoreS3;
import com.io7m.azeno.model.AzUnit;
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.tests.containers.AzDatabaseFixture;
import com.io7m.azeno.tests.containers.AzFixtures;
import com.io7m.darco.api.DDatabaseException;
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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({ErvillaExtension.class, ZeladorExtension.class})
@ErvillaConfiguration(projectName = "com.io7m.azeno", disabledIfUnsupported = true)
public final class AzDatabaseCollectionsTest
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
  }

  /**
   * Creating collections works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testCollectionCreate0()
    throws Exception
  {
    final var user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    final var store =
      new AzStoreS3(
        AzStoreID.random(),
        "Store 1",
        "us-east-1",
        URI.create("https://s3.example.com"),
        Optional.of(
          new HClientAccessKeys("abcd", "1234")
        )
      );

    final var collection =
      new AzCollection(
        AzCollectionID.random(),
        "Collection 1",
        store.id(),
        this.schema.id()
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);
    this.storePut.execute(store);
    this.schemaPut.execute(this.schema);
    this.collectionPut.execute(collection);
    assertEquals(
      collection,
      this.collectionGet.execute(collection.id()).orElseThrow());
  }

  /**
   * Creating collections works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testCollectionCreate1()
    throws Exception
  {
    final var user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    final var store =
      new AzStoreS3(
        AzStoreID.random(),
        "Store 1",
        "us-east-1",
        URI.create("https://s3.example.com"),
        Optional.of(
          new HClientAccessKeys("abcd", "1234")
        )
      );

    final var collection0 =
      new AzCollection(
        AzCollectionID.random(),
        "Collection 1",
        store.id(),
        this.schema.id()
      );

    final var collection1 =
      new AzCollection(
        collection0.id(),
        "Collection 2",
        store.id(),
        this.schema.id()
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);
    this.storePut.execute(store);
    this.schemaPut.execute(this.schema);
    this.collectionPut.execute(collection0);
    assertEquals(
      collection0,
      this.collectionGet.execute(collection0.id()).orElseThrow());
    this.collectionPut.execute(collection1);
    assertEquals(
      collection1,
      this.collectionGet.execute(collection0.id()).orElseThrow());
  }

  /**
   * Creating collections works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testCollectionCreate2()
    throws Exception
  {
    final var user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    final var store =
      new AzStoreS3(
        AzStoreID.random(),
        "Store 1",
        "us-east-1",
        URI.create("https://s3.example.com"),
        Optional.of(
          new HClientAccessKeys("abcd", "1234")
        )
      );

    final var collection0 =
      new AzCollection(
        AzCollectionID.random(),
        "Collection 1",
        store.id(),
        this.schema.id()
      );

    final var collection1 =
      new AzCollection(
        collection0.id(),
        "Collection 2",
        store.id(),
        this.schema.id()
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);
    this.storePut.execute(store);
    this.schemaPut.execute(this.schema);
    this.collectionPut.execute(collection0);
    assertEquals(
      collection0,
      this.collectionGet.execute(collection0.id()).orElseThrow());
    this.collectionPut.execute(collection1);
    assertEquals(
      collection1,
      this.collectionGet.execute(collection0.id()).orElseThrow());

    {
      final var s =
        this.collectionSearch.execute(AzUnit.UNIT);
      final var p0 =
        s.pageCurrent(this.transaction);
      final var p1 =
        s.pageNext(this.transaction);
      final var p2 =
        s.pagePrevious(this.transaction);

      assertEquals(List.of(collection1), p0.items());
      assertEquals(List.of(collection1), p1.items());
      assertEquals(List.of(collection1), p2.items());
    }
  }

  /**
   * Nonexistent stores are nonexistent.
   *
   * @throws Exception On errors
   */

  @Test
  public void testCollectionGet0()
    throws Exception
  {
    assertEquals(
      Optional.empty(),
      this.collectionGet.execute(AzCollectionID.random()));
  }

  /**
   * Creating collections works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testCollectionCreateNonexistentStore0()
    throws Exception
  {
    final var user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    final var collection =
      new AzCollection(
        AzCollectionID.random(),
        "Collection 1",
        AzStoreID.random(),
        this.schema.id()
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);
    this.schemaPut.execute(this.schema);

    {
      final var e =
        assertThrows(DDatabaseException.class, () -> {
          this.collectionPut.execute(collection);
        });
      assertEquals(AzStandardErrorCodes.errorNonexistent().id(), e.errorCode());
    }
  }

  /**
   * Creating collections works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testCollectionCreateNonexistentSchema0()
    throws Exception
  {
    final var user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    final var store =
      new AzStoreS3(
        AzStoreID.random(),
        "Store 1",
        "us-east-1",
        URI.create("https://s3.example.com"),
        Optional.of(
          new HClientAccessKeys("abcd", "1234")
        )
      );

    final var collection =
      new AzCollection(
        AzCollectionID.random(),
        "Collection 1",
        AzStoreID.random(),
        this.schema.id()
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);
    this.storePut.execute(store);

    {
      final var e =
        assertThrows(DDatabaseException.class, () -> {
          this.collectionPut.execute(collection);
        });
      assertEquals(AzStandardErrorCodes.errorNonexistent().id(), e.errorCode());
    }
  }

  /**
   * Setting access for a collection works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testCollectionAccessSet0()
    throws Exception
  {
    final var user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    final var store =
      new AzStoreS3(
        AzStoreID.random(),
        "Store 1",
        "us-east-1",
        URI.create("https://s3.example.com"),
        Optional.of(
          new HClientAccessKeys("abcd", "1234")
        )
      );

    final var collection =
      new AzCollection(
        AzCollectionID.random(),
        "Collection 1",
        store.id(),
        this.schema.id()
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);
    this.storePut.execute(store);
    this.schemaPut.execute(this.schema);
    this.collectionPut.execute(collection);

    final var access =
      new AzCollectionAccess(
        user.userId(),
        collection.id(),
        true, true
      );
    this.collectionAccessSet.execute(access);
    assertEquals(access, this.collectionAccessGet.execute(access.request()));
  }

  /**
   * Nonexistent collections or users return a default access value.
   *
   * @throws Exception On errors
   */

  @Test
  public void testCollectionAccessSet1()
    throws Exception
  {
    final var user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    final var store =
      new AzStoreS3(
        AzStoreID.random(),
        "Store 1",
        "us-east-1",
        URI.create("https://s3.example.com"),
        Optional.of(
          new HClientAccessKeys("abcd", "1234")
        )
      );

    final var collection =
      new AzCollection(
        AzCollectionID.random(),
        "Collection 1",
        store.id(),
        this.schema.id()
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);

    final var access =
      new AzCollectionAccess(
        user.userId(),
        collection.id(),
        false,
        false
      );

    assertEquals(access, this.collectionAccessGet.execute(access.request()));
  }

  /**
   * Nonexistent collections or users cannot have their access set.
   *
   * @throws Exception On errors
   */

  @Test
  public void testCollectionAccessSetError0()
    throws Exception
  {
    final var user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    final var store =
      new AzStoreS3(
        AzStoreID.random(),
        "Store 1",
        "us-east-1",
        URI.create("https://s3.example.com"),
        Optional.of(
          new HClientAccessKeys("abcd", "1234")
        )
      );

    final var collection =
      new AzCollection(
        AzCollectionID.random(),
        "Collection 1",
        store.id(),
        this.schema.id()
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);

    {
      final var access =
        new AzCollectionAccess(
          user.userId(),
          collection.id(),
          false,
          false
        );

      final var e =
        assertThrows(DDatabaseException.class, () -> {
          this.collectionAccessSet.execute(access);
        });
      assertEquals(AzStandardErrorCodes.errorNonexistent().id(), e.errorCode());
    }

    this.storePut.execute(store);
    this.schemaPut.execute(this.schema);
    this.collectionPut.execute(collection);

    {
      final var access =
        new AzCollectionAccess(
          AzUserID.random(),
          collection.id(),
          false,
          false
        );

      final var e =
        assertThrows(DDatabaseException.class, () -> {
          this.collectionAccessSet.execute(access);
        });
      assertEquals(AzStandardErrorCodes.errorNonexistent().id(), e.errorCode());
    }
  }

  /**
   * Collection names must be unique.
   *
   * @throws Exception On errors
   */

  @Test
  public void testCollectionCreateUnique()
    throws Exception
  {
    final var user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    final var store =
      new AzStoreS3(
        AzStoreID.random(),
        "Store 1",
        "us-east-1",
        URI.create("https://s3.example.com"),
        Optional.of(
          new HClientAccessKeys("abcd", "1234")
        )
      );

    final var collection0 =
      new AzCollection(
        AzCollectionID.random(),
        "Collection 1",
        store.id(),
        this.schema.id()
      );

    final var collection1 =
      new AzCollection(
        collection0.id(),
        "Collection 2",
        store.id(),
        this.schema.id()
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);
    this.storePut.execute(store);
    this.schemaPut.execute(this.schema);
    this.collectionPut.execute(collection0);
    assertEquals(
      collection0,
      this.collectionGet.execute(collection0.id()).orElseThrow());
    this.collectionPut.execute(collection1);
    assertEquals(
      collection1,
      this.collectionGet.execute(collection0.id()).orElseThrow());

    {
      final var s =
        this.collectionSearch.execute(AzUnit.UNIT);
      final var p0 =
        s.pageCurrent(this.transaction);
      final var p1 =
        s.pageNext(this.transaction);
      final var p2 =
        s.pagePrevious(this.transaction);

      assertEquals(List.of(collection1), p0.items());
      assertEquals(List.of(collection1), p1.items());
      assertEquals(List.of(collection1), p2.items());
    }
  }
}
