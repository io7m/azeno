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

import com.io7m.azeno.database.api.AzDatabaseConnectionType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.database.api.AzDatabaseType;
import com.io7m.azeno.database.api.AzUserGetType;
import com.io7m.azeno.database.api.AzUserPutType;
import com.io7m.azeno.database.api.AzUserSearchType;
import com.io7m.azeno.model.AzUnit;
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.tests.containers.AzDatabaseFixture;
import com.io7m.azeno.tests.containers.AzFixtures;
import com.io7m.ervilla.api.EContainerSupervisorType;
import com.io7m.ervilla.test_extension.ErvillaCloseAfterSuite;
import com.io7m.ervilla.test_extension.ErvillaConfiguration;
import com.io7m.ervilla.test_extension.ErvillaExtension;
import com.io7m.idstore.model.IdName;
import com.io7m.medrina.api.MRoleName;
import com.io7m.medrina.api.MSubject;
import com.io7m.zelador.test_extension.CloseableResourcesType;
import com.io7m.zelador.test_extension.ZeladorExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({ErvillaExtension.class, ZeladorExtension.class})
@ErvillaConfiguration(projectName = "com.io7m.azeno", disabledIfUnsupported = true)
public final class AzDatabaseUsersTest
{
  private static AzDatabaseFixture DATABASE_FIXTURE;
  private AzDatabaseConnectionType connection;
  private AzDatabaseTransactionType transaction;
  private AzDatabaseType database;
  private AzUserSearchType userSearch;
  private AzUserGetType userGet;
  private AzUserPutType userPut;

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

    this.userSearch =
      this.transaction.query(AzUserSearchType.class);
    this.userGet =
      this.transaction.query(AzUserGetType.class);
    this.userPut =
      this.transaction.query(AzUserPutType.class);
  }

  /**
   * Creating users works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testUserCreate0()
    throws Exception
  {
    final var user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);
    assertEquals(user, this.userGet.execute(user.userId()).orElseThrow());
  }

  /**
   * Creating users works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testUserCreate1()
    throws Exception
  {
    final var user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of(
          MRoleName.of("role0"),
          MRoleName.of("role1"),
          MRoleName.of("role2")
        ))
      );

    this.transaction.setUserID(user.userId());
    this.userPut.execute(user);
    assertEquals(user, this.userGet.execute(user.userId()).orElseThrow());

    {
      final var s =
        this.userSearch.execute(AzUnit.UNIT);
      final var p0 =
        s.pageCurrent(this.transaction);
      final var p1 =
        s.pageNext(this.transaction);
      final var p2 =
        s.pagePrevious(this.transaction);

      assertEquals(List.of(user.withoutRoles()), p0.items());
      assertEquals(List.of(user.withoutRoles()), p1.items());
      assertEquals(List.of(user.withoutRoles()), p2.items());
    }
  }

  /**
   * Nonexistent users are nonexistent.
   *
   * @throws Exception On errors
   */

  @Test
  public void testUserGet0()
    throws Exception
  {
    assertEquals(
      Optional.empty(),
      this.userGet.execute(AzUserID.random())
    );
  }
}
