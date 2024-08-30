/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.azeno.tests.server.controller;

import com.io7m.azeno.database.api.AzUserGetType;
import com.io7m.azeno.database.api.AzUserPutType;
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.protocol.asset.AzACommandRolesRevoke;
import com.io7m.azeno.server.controller.asset.AzACmdRolesRevoke;
import com.io7m.azeno.server.controller.command_exec.AzCommandExecutionFailure;
import com.io7m.idstore.model.IdName;
import com.io7m.medrina.api.MSubject;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorNonexistent;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorOperationNotPermitted;
import static com.io7m.azeno.security.AzSecurityPolicy.ROLE_ASSET_ADMIN;
import static com.io7m.azeno.security.AzSecurityPolicy.ROLE_AUDIT_READER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @see AzACmdRolesRevoke
 */

public final class AzACmdRolesRevokeTest
  extends AzCmdAbstractContract
{
  /**
   * It's not possible to take away a role you don't have.
   *
   * @throws Exception On errors
   */

  @Test
  public void testNotAllowed0()
    throws Exception
  {
    /* Arrange. */

    final var userGet =
      mock(AzUserGetType.class);
    final var transaction =
      this.transaction();

    when(transaction.query(AzUserGetType.class))
      .thenReturn(userGet);

    final var context =
      this.createContext();

    /* Act. */

    final var handler =
      new AzACmdRolesRevoke();
    final var ex =
      assertThrows(AzCommandExecutionFailure.class, () -> {
        handler.execute(
          context,
          new AzACommandRolesRevoke(
            AzUserID.random(),
            Set.of(ROLE_AUDIT_READER))
        );
      });

    /* Assert. */

    assertEquals(errorOperationNotPermitted(), ex.errorCode());
  }

  /**
   * Trying to update a nonexistent user fails.
   *
   * @throws Exception On errors
   */

  @Test
  public void testNonexistent()
    throws Exception
  {
    /* Arrange. */

    final var userGet =
      mock(AzUserGetType.class);
    final var transaction =
      this.transaction();

    when(transaction.query(AzUserGetType.class))
      .thenReturn(userGet);

    when(userGet.execute(any()))
      .thenReturn(Optional.empty());

    this.setRoles(ROLE_AUDIT_READER);

    final var context =
      this.createContext();

    /* Act. */

    final var handler = new AzACmdRolesRevoke();
    final var ex =
      assertThrows(AzCommandExecutionFailure.class, () -> {
        handler.execute(
          context,
          new AzACommandRolesRevoke(
            AzUserID.random(),
            Set.of(ROLE_AUDIT_READER))
        );
      });

    /* Assert. */

    assertEquals(errorNonexistent(), ex.errorCode());
  }

  /**
   * Taking away a held role works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testRoleGiveaway()
    throws Exception
  {
    /* Arrange. */

    final var userGet =
      mock(AzUserGetType.class);
    final var userPut =
      mock(AzUserPutType.class);
    final var transaction =
      this.transaction();

    final var targetUser =
      AzUserID.random();

    when(transaction.query(AzUserGetType.class))
      .thenReturn(userGet);
    when(transaction.query(AzUserPutType.class))
      .thenReturn(userPut);

    when(userGet.execute(targetUser))
      .thenReturn(Optional.of(
        new AzUser(
          targetUser,
          new IdName("x"),
          new MSubject(Set.of())
        )
      ));

    this.setRoles(ROLE_AUDIT_READER);

    final var context =
      this.createContext();

    /* Act. */

    final var handler = new AzACmdRolesRevoke();
    handler.execute(
      context,
      new AzACommandRolesRevoke(targetUser, Set.of(ROLE_AUDIT_READER))
    );

    /* Assert. */

    verify(transaction)
      .query(AzUserGetType.class);
    verify(transaction)
      .query(AzUserPutType.class);

    verify(userGet)
      .execute(targetUser);
    verify(userPut)
      .execute(
        new AzUser(targetUser, new IdName("x"), new MSubject(Set.of()))
      );

    verifyNoMoreInteractions(transaction);
    verifyNoMoreInteractions(userGet);
    verifyNoMoreInteractions(userPut);
  }

  /**
   * Taking away any role works for an admin.
   *
   * @throws Exception On errors
   */

  @Test
  public void testRoleGiveawayAdmin()
    throws Exception
  {
    /* Arrange. */

    final var userGet =
      mock(AzUserGetType.class);
    final var userPut =
      mock(AzUserPutType.class);
    final var transaction =
      this.transaction();

    final var targetUser =
      AzUserID.random();

    when(transaction.query(AzUserGetType.class))
      .thenReturn(userGet);
    when(transaction.query(AzUserPutType.class))
      .thenReturn(userPut);

    when(userGet.execute(targetUser))
      .thenReturn(Optional.of(
        new AzUser(
          targetUser,
          new IdName("x"),
          new MSubject(Set.of())
        )
      ));

    this.setRoles(ROLE_ASSET_ADMIN);

    final var context =
      this.createContext();

    /* Act. */

    final var handler = new AzACmdRolesRevoke();
    handler.execute(
      context,
      new AzACommandRolesRevoke(targetUser, Set.of(ROLE_AUDIT_READER))
    );

    /* Assert. */

    verify(transaction)
      .query(AzUserGetType.class);
    verify(transaction)
      .query(AzUserPutType.class);

    verify(userGet)
      .execute(targetUser);
    verify(userPut)
      .execute(
        new AzUser(targetUser, new IdName("x"), new MSubject(Set.of()))
      );

    verifyNoMoreInteractions(transaction);
    verifyNoMoreInteractions(userGet);
    verifyNoMoreInteractions(userPut);
  }
}
