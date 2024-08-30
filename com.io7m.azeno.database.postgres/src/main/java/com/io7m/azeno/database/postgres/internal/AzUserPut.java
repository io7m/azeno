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


package com.io7m.azeno.database.postgres.internal;

import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.database.api.AzUserPutType;
import com.io7m.azeno.model.AzAuditEvent;
import com.io7m.azeno.model.AzUnit;
import com.io7m.azeno.model.AzUser;
import com.io7m.medrina.api.MRoleName;
import com.io7m.medrina.api.MSubject;
import org.jooq.DSLContext;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static com.io7m.azeno.database.postgres.internal.Tables.USERS;

/**
 * UserPut.
 */

public final class AzUserPut
  extends AzDatabaseQueryAbstract<AzUser, AzUnit>
  implements AzUserPutType
{
  AzUserPut(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  /**
   * @return The query provider
   */

  public static AzDatabaseQueryProviderType<AzUser, AzUnit, AzUserPutType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzUserPutType.class,
      AzUserPut::new
    );
  }

  private static String[] rolesOf(
    final MSubject subject)
  {
    return roleSetToStringArray(subject.roles());
  }

  private static String[] roleSetToStringArray(
    final Set<MRoleName> targetRoles)
  {
    final var roles = new String[targetRoles.size()];
    var index = 0;
    for (final var role : targetRoles) {
      roles[index] = role.value().value();
      ++index;
    }
    Arrays.sort(roles);
    return roles;
  }

  @Override
  protected AzUnit onExecute(
    final AzDatabaseTransactionType transaction,
    final AzUser user)
  {
    final var context =
      transaction.get(DSLContext.class);

    final String[] roles =
      rolesOf(user.subject());

    context.insertInto(USERS)
      .set(USERS.ID, user.userId().id())
      .set(USERS.NAME, user.name().value())
      .set(USERS.ROLES, roles)
      .onDuplicateKeyUpdate()
      .set(USERS.NAME, user.name().value())
      .set(USERS.ROLES, roles)
      .execute();

    putAuditEvent(
      context,
      new AzAuditEvent(
        0L,
        OffsetDateTime.now(),
        user.userId(),
        "USER_UPDATED",
        Map.of()
      )
    );

    return AzUnit.UNIT;
  }
}
