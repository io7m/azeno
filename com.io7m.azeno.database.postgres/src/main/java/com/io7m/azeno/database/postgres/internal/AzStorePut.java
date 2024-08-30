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
import com.io7m.azeno.database.api.AzStorePutType;
import com.io7m.azeno.model.AzAuditEvent;
import com.io7m.azeno.model.AzStoreS3;
import com.io7m.azeno.model.AzStoreType;
import com.io7m.azeno.model.AzUnit;
import com.io7m.darco.api.DDatabaseException;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.postgres.extensions.bindings.HstoreBinding;
import org.jooq.postgres.extensions.types.Hstore;

import java.time.OffsetDateTime;
import java.util.HashMap;

import static com.io7m.azeno.database.postgres.internal.Tables.STORES;

/**
 * StorePut.
 */

public final class AzStorePut
  extends AzDatabaseQueryAbstract<AzStoreType, AzUnit>
  implements AzStorePutType
{
  private static final DataType<Hstore> STORE_CONFIGURATION_DATA_TYPE =
    SQLDataType.OTHER.asConvertedDataType(new HstoreBinding());

  static final Field<Hstore> STORE_CONFIGURATION =
    DSL.field("STORE_CONFIGURATION", STORE_CONFIGURATION_DATA_TYPE);

  AzStorePut(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  /**
   * @return The query provider
   */

  public static AzDatabaseQueryProviderType<AzStoreType, AzUnit, AzStorePutType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzStorePutType.class,
      AzStorePut::new
    );
  }

  @Override
  protected AzUnit onExecute(
    final AzDatabaseTransactionType transaction,
    final AzStoreType store)
    throws DDatabaseException
  {
    this.putAttribute("StoreID", store.id());

    final var context =
      transaction.get(DSLContext.class);

    context.insertInto(STORES)
      .set(STORES.STORE_ID, store.id().id())
      .set(STORES.STORE_TITLE, store.title())
      .set(STORES.STORE_TYPE, store.kind().name())
      .set(STORE_CONFIGURATION, serializeStore(store))
      .onDuplicateKeyUpdate()
      .set(STORES.STORE_TITLE, store.title())
      .set(STORES.STORE_TYPE, store.kind().name())
      .set(STORE_CONFIGURATION, serializeStore(store))
      .execute();

    putAuditEvent(
      context,
      new AzAuditEvent(
        0L,
        OffsetDateTime.now(),
        transaction.userId(),
        "STORE_UPDATED",
        this.attributes()
      ));

    return AzUnit.UNIT;
  }

  private static Hstore serializeStore(
    final AzStoreType store)
  {
    return switch (store) {
      case final AzStoreS3 s3 -> {
        final var map = new HashMap<String, String>();
        map.put("region", s3.region());
        map.put("endpoint", s3.endpoint().toString());
        s3.credentials().ifPresent(ck -> {
          map.put("accessKey", ck.accessKey());
          map.put("secret", ck.secret());
        });
        yield Hstore.hstore(map);
      }
    };
  }
}
