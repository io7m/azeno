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
import com.io7m.azeno.database.api.AzStoreGetType;
import com.io7m.azeno.database.api.AzStorePutType;
import com.io7m.azeno.database.api.AzStoreSearchType;
import com.io7m.azeno.database.api.AzUserPutType;
import com.io7m.azeno.model.AzStoreID;
import com.io7m.azeno.model.AzStoreS3;
import com.io7m.azeno.model.AzUnit;
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.tests.containers.AzDatabaseFixture;
import com.io7m.azeno.tests.containers.AzFixtures;
import com.io7m.ervilla.api.EContainerSupervisorType;
import com.io7m.ervilla.test_extension.ErvillaCloseAfterSuite;
import com.io7m.ervilla.test_extension.ErvillaConfiguration;
import com.io7m.ervilla.test_extension.ErvillaExtension;
import com.io7m.huanuco.api.HClientAccessKeys;
import com.io7m.idstore.model.IdName;
import com.io7m.medrina.api.MSubject;
import com.io7m.zelador.test_extension.CloseableResourcesType;
import com.io7m.zelador.test_extension.ZeladorExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({ErvillaExtension.class, ZeladorExtension.class})
@ErvillaConfiguration(projectName = "com.io7m.azeno", disabledIfUnsupported = true)
public final class AzDatabaseStoresTest
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
  private AzStoreSearchType storeSearch;

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

    this.storePut =
      this.transaction.query(AzStorePutType.class);
    this.storeGet =
      this.transaction.query(AzStoreGetType.class);
    this.storeSearch =
      this.transaction.query(AzStoreSearchType.class);
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
   * Creating stores works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testStoreCreate0()
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

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);
    this.storePut.execute(store);
    assertEquals(store, this.storeGet.execute(store.id()).orElseThrow());
  }

  /**
   * Creating stores works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testStoreCreate1()
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
        Optional.empty()
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);
    this.storePut.execute(store);
    assertEquals(store, this.storeGet.execute(store.id()).orElseThrow());
  }

  /**
   * Creating stores works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testStoreCreate2()
    throws Exception
  {
    final var user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    final var store0 =
      new AzStoreS3(
        AzStoreID.random(),
        "Store 1",
        "us-east-1",
        URI.create("https://s3.example.com"),
        Optional.of(
          new HClientAccessKeys("abcd", "1234")
        )
      );

    final var store1 =
      new AzStoreS3(
        store0.id(),
        "Store 1",
        "us-east-1",
        URI.create("https://s3.example.com"),
        Optional.empty()
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);
    this.storePut.execute(store0);
    assertEquals(store0, this.storeGet.execute(store0.id()).orElseThrow());
    this.storePut.execute(store1);
    assertEquals(store1, this.storeGet.execute(store0.id()).orElseThrow());

    {
      final var s =
        this.storeSearch.execute(AzUnit.UNIT);
      final var p0 =
        s.pageCurrent(this.transaction);
      final var p1 =
        s.pageNext(this.transaction);
      final var p2 =
        s.pagePrevious(this.transaction);

      assertEquals(List.of(store1.summary()), p0.items());
      assertEquals(List.of(store1.summary()), p1.items());
      assertEquals(List.of(store1.summary()), p2.items());
    }
  }

  /**
   * Nonexistent stores are nonexistent.
   *
   * @throws Exception On errors
   */

  @Test
  public void testStoreGet0()
    throws Exception
  {
    final var storeGet =
      this.transaction.query(AzStoreGetType.class);

    assertEquals(Optional.empty(), storeGet.execute(AzStoreID.random()));
  }
}
