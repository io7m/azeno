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

import com.io7m.azeno.database.api.AzCollectionGetType;
import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.model.AzCollection;
import com.io7m.azeno.model.AzCollectionID;
import com.io7m.azeno.model.AzSchemaID;
import com.io7m.azeno.model.AzStoreID;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.lanark.core.RDottedName;
import org.jooq.DSLContext;

import java.util.Optional;

import static com.io7m.azeno.database.postgres.internal.Tables.COLLECTIONS;
import static com.io7m.azeno.database.postgres.internal.Tables.SCHEMAS;

/**
 * CollectionGet.
 */

public final class AzCollectionGet
  extends AzDatabaseQueryAbstract<AzCollectionID, Optional<AzCollection>>
  implements AzCollectionGetType
{
  AzCollectionGet(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  /**
   * @return The query provider
   */

  public static AzDatabaseQueryProviderType<AzCollectionID, Optional<AzCollection>, AzCollectionGetType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzCollectionGetType.class,
      AzCollectionGet::new
    );
  }

  @Override
  protected Optional<AzCollection> onExecute(
    final AzDatabaseTransactionType transaction,
    final AzCollectionID id)
    throws DDatabaseException
  {
    this.putAttribute("CollectionID", id);

    final var context =
      transaction.get(DSLContext.class);

    return context.select(
        COLLECTIONS.COLLECTION_ID,
        COLLECTIONS.COLLECTION_TITLE,
        COLLECTIONS.COLLECTION_STORE,
        SCHEMAS.SCHEMA_NAME,
        SCHEMAS.SCHEMA_VERSION)
      .from(COLLECTIONS)
      .join(SCHEMAS)
      .on(SCHEMAS.SCHEMA_ID.eq(COLLECTIONS.COLLECTION_SCHEMA))
      .where(COLLECTIONS.COLLECTION_ID.eq(id.id()))
      .fetchOptional()
      .map(this::mapRecord);
  }

  private AzCollection mapRecord(
    final org.jooq.Record x)
  {
    return new AzCollection(
      new AzCollectionID(x.get(COLLECTIONS.COLLECTION_ID)),
      x.get(COLLECTIONS.COLLECTION_TITLE),
      new AzStoreID(x.get(COLLECTIONS.COLLECTION_STORE)),
      new AzSchemaID(
        new RDottedName(x.get(SCHEMAS.SCHEMA_NAME)),
        x.get(SCHEMAS.SCHEMA_VERSION)
      )
    );
  }
}
