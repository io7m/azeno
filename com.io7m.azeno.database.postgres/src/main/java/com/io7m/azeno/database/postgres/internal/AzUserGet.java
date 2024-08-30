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
import com.io7m.azeno.database.api.AzUserGetType;
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.model.AzUserID;
import com.io7m.idstore.model.IdName;
import com.io7m.medrina.api.MRoleName;
import com.io7m.medrina.api.MSubject;
import org.jooq.DSLContext;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.io7m.azeno.database.postgres.internal.Tables.USERS;

/**
 * UserGet.
 */

public final class AzUserGet
  extends AzDatabaseQueryAbstract<AzUserID, Optional<AzUser>>
  implements AzUserGetType
{
  AzUserGet(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  /**
   * @return The query provider
   */

  public static
  AzDatabaseQueryProviderType<AzUserID, Optional<AzUser>, AzUserGetType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzUserGetType.class,
      AzUserGet::new
    );
  }

  @Override
  protected Optional<AzUser> onExecute(
    final AzDatabaseTransactionType transaction,
    final AzUserID id)
  {
    final var context =
      transaction.get(DSLContext.class);

    return context.select(
        USERS.ID,
        USERS.NAME,
        USERS.ROLES)
      .from(USERS)
      .where(USERS.ID.eq(id.id()))
      .fetchOptional()
      .map(this::mapRecord);
  }

  private AzUser mapRecord(
    final org.jooq.Record x)
  {
    return new AzUser(
      new AzUserID(x.get(USERS.ID)),
      new IdName(x.get(USERS.NAME)),
      new MSubject(
        Stream.of(x.get(USERS.ROLES))
          .map(MRoleName::of)
          .collect(Collectors.toSet())
      )
    );
  }
}
