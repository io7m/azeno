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

import com.io7m.azeno.database.api.AzCollectionAccessGetType;
import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.model.AzCollectionAccess;
import com.io7m.azeno.model.AzCollectionAccessRequest;
import com.io7m.azeno.model.AzCollectionID;
import com.io7m.azeno.model.AzUserID;
import com.io7m.darco.api.DDatabaseException;
import org.jooq.DSLContext;

import static com.io7m.azeno.database.postgres.internal.Tables.COLLECTIONS_ACCESS;

/**
 * CollectionAccessGet.
 */

public final class AzCollectionAccessGet
  extends AzDatabaseQueryAbstract<AzCollectionAccessRequest, AzCollectionAccess>
  implements AzCollectionAccessGetType
{
  AzCollectionAccessGet(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  /**
   * @return The query provider
   */

  public static AzDatabaseQueryProviderType<AzCollectionAccessRequest, AzCollectionAccess, AzCollectionAccessGetType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzCollectionAccessGetType.class,
      AzCollectionAccessGet::new
    );
  }

  @Override
  protected AzCollectionAccess onExecute(
    final AzDatabaseTransactionType transaction,
    final AzCollectionAccessRequest access)
    throws DDatabaseException
  {
    this.putAttribute("CollectionID", access.collection().id());
    this.putAttribute("UserID", access.user().id());

    final var context =
      transaction.get(DSLContext.class);

    final var collectionMatches =
      COLLECTIONS_ACCESS.ACCESS_COLLECTION_ID.eq(access.collection().id());
    final var userMatches =
      COLLECTIONS_ACCESS.ACCESS_USER_ID.eq(access.user().id());

    return context.select(
      COLLECTIONS_ACCESS.ACCESS_COLLECTION_ID,
      COLLECTIONS_ACCESS.ACCESS_USER_ID,
      COLLECTIONS_ACCESS.ACCESS_READ,
      COLLECTIONS_ACCESS.ACCESS_WRITE
    ).from(COLLECTIONS_ACCESS)
      .where(collectionMatches.and(userMatches))
      .fetchOptional()
      .map(this::mapRecord)
      .orElseGet(access::noAccess);
  }

  private AzCollectionAccess mapRecord(
    final org.jooq.Record r)
  {
    return new AzCollectionAccess(
      new AzUserID(
        r.get(COLLECTIONS_ACCESS.ACCESS_USER_ID)),
      new AzCollectionID(
        r.get(COLLECTIONS_ACCESS.ACCESS_COLLECTION_ID)),
      r.get(COLLECTIONS_ACCESS.ACCESS_READ),
      r.get(COLLECTIONS_ACCESS.ACCESS_WRITE)
    );
  }
}
