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

import com.io7m.azeno.database.api.AzCollectionAccessSetType;
import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.model.AzAuditEvent;
import com.io7m.azeno.model.AzCollectionAccess;
import com.io7m.azeno.model.AzUnit;
import com.io7m.darco.api.DDatabaseException;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import java.time.OffsetDateTime;

import static com.io7m.azeno.database.postgres.internal.AZDatabaseExceptions.handleDatabaseException;
import static com.io7m.azeno.database.postgres.internal.Tables.COLLECTIONS_ACCESS;

/**
 * CollectionAccessSet.
 */

public final class AzCollectionAccessSet
  extends AzDatabaseQueryAbstract<AzCollectionAccess, AzUnit>
  implements AzCollectionAccessSetType
{
  AzCollectionAccessSet(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  /**
   * @return The query provider
   */

  public static AzDatabaseQueryProviderType<AzCollectionAccess, AzUnit, AzCollectionAccessSetType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzCollectionAccessSetType.class,
      AzCollectionAccessSet::new
    );
  }

  @Override
  protected AzUnit onExecute(
    final AzDatabaseTransactionType transaction,
    final AzCollectionAccess access)
    throws DDatabaseException
  {
    this.putAttribute("CollectionID", access.collection().id());
    this.putAttribute("UserID", access.user().id());

    final var context =
      transaction.get(DSLContext.class);

    try {
      context.insertInto(COLLECTIONS_ACCESS)
        .set(COLLECTIONS_ACCESS.ACCESS_COLLECTION_ID, access.collection().id())
        .set(COLLECTIONS_ACCESS.ACCESS_USER_ID, access.user().id())
        .set(COLLECTIONS_ACCESS.ACCESS_READ, access.read())
        .set(COLLECTIONS_ACCESS.ACCESS_WRITE, access.read())
        .onDuplicateKeyUpdate()
        .set(COLLECTIONS_ACCESS.ACCESS_COLLECTION_ID, access.collection().id())
        .set(COLLECTIONS_ACCESS.ACCESS_USER_ID, access.user().id())
        .set(COLLECTIONS_ACCESS.ACCESS_READ, access.read())
        .set(COLLECTIONS_ACCESS.ACCESS_WRITE, access.read())
        .execute();

      putAuditEvent(
        context,
        new AzAuditEvent(
          0L,
          OffsetDateTime.now(),
          transaction.userId(),
          "COLLECTION_ACCESS_UPDATED",
          this.attributes()
        ));

      return AzUnit.UNIT;
    } catch (final DataAccessException e) {
      throw handleDatabaseException(transaction, this.attributes(), e);
    }
  }
}
