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

import com.io7m.azeno.database.api.AzCollectionPutType;
import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.model.AzAuditEvent;
import com.io7m.azeno.model.AzCollection;
import com.io7m.azeno.model.AzUnit;
import com.io7m.darco.api.DDatabaseException;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import java.time.OffsetDateTime;

import static com.io7m.azeno.database.postgres.internal.AZDatabaseExceptions.handleDatabaseException;
import static com.io7m.azeno.database.postgres.internal.Tables.COLLECTIONS;
import static com.io7m.azeno.database.postgres.internal.Tables.SCHEMAS;

/**
 * CollectionPut.
 */

public final class AzCollectionPut
  extends AzDatabaseQueryAbstract<AzCollection, AzUnit>
  implements AzCollectionPutType
{
  AzCollectionPut(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  /**
   * @return The query provider
   */

  public static AzDatabaseQueryProviderType<AzCollection, AzUnit, AzCollectionPutType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzCollectionPutType.class,
      AzCollectionPut::new
    );
  }

  @Override
  protected AzUnit onExecute(
    final AzDatabaseTransactionType transaction,
    final AzCollection collection)
    throws DDatabaseException
  {
    this.putAttribute("CollectionID", collection.id());

    final var context =
      transaction.get(DSLContext.class);

    try {
      final var schema =
        collection.schema();

      final var schemaMatches =
        SCHEMAS.SCHEMA_NAME.eq(schema.name().value())
          .and(SCHEMAS.SCHEMA_VERSION.eq(schema.version()));

      final var schemaId =
        context.select(SCHEMAS.SCHEMA_ID)
          .from(SCHEMAS)
          .where(schemaMatches);

      context.insertInto(COLLECTIONS)
        .set(COLLECTIONS.COLLECTION_ID, collection.id().id())
        .set(COLLECTIONS.COLLECTION_TITLE, collection.title())
        .set(COLLECTIONS.COLLECTION_STORE, collection.store().id())
        .set(COLLECTIONS.COLLECTION_SCHEMA, schemaId)
        .onDuplicateKeyUpdate()
        .set(COLLECTIONS.COLLECTION_TITLE, collection.title())
        .set(COLLECTIONS.COLLECTION_STORE, collection.store().id())
        .set(COLLECTIONS.COLLECTION_SCHEMA, schemaId)
        .execute();

      putAuditEvent(
        context,
        new AzAuditEvent(
          0L,
          OffsetDateTime.now(),
          transaction.userId(),
          "COLLECTION_UPDATED",
          this.attributes()
        ));

      return AzUnit.UNIT;
    } catch (final DataAccessException e) {
      throw handleDatabaseException(transaction, this.attributes(), e);
    }
  }
}
