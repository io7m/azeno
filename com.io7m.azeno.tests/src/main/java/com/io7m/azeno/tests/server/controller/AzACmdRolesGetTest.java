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
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.protocol.asset.AzACommandRolesGet;
import com.io7m.azeno.protocol.asset.AzAResponseRolesGet;
import com.io7m.azeno.server.controller.asset.AzACmdRolesGet;
import com.io7m.azeno.server.controller.command_exec.AzCommandExecutionFailure;
import com.io7m.idstore.model.IdName;
import com.io7m.medrina.api.MSubject;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorNonexistent;
import static com.io7m.azeno.security.AzSecurityPolicy.ROLE_AUDIT_READER;
import static com.io7m.azeno.security.AzSecurityPolicy.ROLE_PERMISSIONS_DELEGATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @see AzACmdRolesGet
 */

public final class AzACmdRolesGetTest
  extends AzCmdAbstractContract
{
  /**
   * Trying to examine a nonexistent user fails.
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

    final var context =
      this.createContext();

    /* Act. */

    final var handler = new AzACmdRolesGet();
    final var ex =
      assertThrows(AzCommandExecutionFailure.class, () -> {
        handler.execute(
          context,
          new AzACommandRolesGet(AzUserID.random())
        );
      });

    /* Assert. */

    assertEquals(errorNonexistent(), ex.errorCode());
  }

  /**
   * Examining a user that exists works.
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
    final var transaction =
      this.transaction();

    final var targetUser =
      AzUserID.random();

    when(transaction.query(AzUserGetType.class))
      .thenReturn(userGet);

    when(userGet.execute(targetUser))
      .thenReturn(Optional.of(
        new AzUser(
          targetUser,
          new IdName("x"),
          new MSubject(Set.of(
            ROLE_AUDIT_READER,
            ROLE_PERMISSIONS_DELEGATOR
          ))
        )
      ));

    final var context =
      this.createContext();

    /* Act. */

    final var handler =
      new AzACmdRolesGet();
    final var result =
      handler.execute(context, new AzACommandRolesGet(targetUser));
    final var get =
      assertInstanceOf(AzAResponseRolesGet.class, result);

    /* Assert. */

    assertEquals(
      Set.of(
        ROLE_AUDIT_READER,
        ROLE_PERMISSIONS_DELEGATOR
      ),
      get.roles()
    );

    verify(transaction)
      .query(AzUserGetType.class);
    verify(userGet)
      .execute(targetUser);

    verifyNoMoreInteractions(transaction);
    verifyNoMoreInteractions(userGet);
  }
}
