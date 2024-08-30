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
import com.io7m.azeno.database.api.AzStoreGetType;
import com.io7m.azeno.model.AzStoreID;
import com.io7m.azeno.model.AzStoreS3;
import com.io7m.azeno.model.AzStoreType;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.huanuco.api.HClientAccessKeys;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.postgres.extensions.bindings.HstoreBinding;
import org.jooq.postgres.extensions.types.Hstore;

import java.net.URI;
import java.util.Optional;

import static com.io7m.azeno.database.postgres.internal.Tables.STORES;

/**
 * StoreGet.
 */

public final class AzStoreGet
  extends AzDatabaseQueryAbstract<AzStoreID, Optional<AzStoreType>>
  implements AzStoreGetType
{
  private static final DataType<Hstore> STORE_CONFIGURATION_DATA_TYPE =
    SQLDataType.OTHER.asConvertedDataType(new HstoreBinding());

  static final Field<Hstore> STORE_CONFIGURATION =
    DSL.field("STORE_CONFIGURATION", STORE_CONFIGURATION_DATA_TYPE);

  AzStoreGet(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  /**
   * @return The query provider
   */

  public static AzDatabaseQueryProviderType<AzStoreID, Optional<AzStoreType>, AzStoreGetType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzStoreGetType.class,
      AzStoreGet::new
    );
  }

  @Override
  protected Optional<AzStoreType> onExecute(
    final AzDatabaseTransactionType transaction,
    final AzStoreID id)
    throws DDatabaseException
  {
    this.putAttribute("StoreID", id);

    final var context =
      transaction.get(DSLContext.class);

    final var recordOpt =
      context.select(
          STORES.STORE_ID,
          STORES.STORE_TITLE,
          STORES.STORE_TYPE,
          STORE_CONFIGURATION)
        .from(STORES)
        .where(STORES.STORE_ID.eq(id.id()))
        .fetchOptional();

    if (recordOpt.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(this.mapRecord(recordOpt.get()));
  }

  private AzStoreType mapRecord(
    final org.jooq.Record x)
    throws DDatabaseException
  {
    final var type =
      x.get(STORES.STORE_TYPE);

    this.putAttribute("StoreType", type);

    return switch (type) {
      case "S3" -> {
        final var obj =
          x.get(STORE_CONFIGURATION).data();

        final var region =
          obj.get("region");
        final var endpoint =
          URI.create(obj.get("endpoint"));

        final var accessKey =
          obj.get("accessKey");
        final var secret =
          obj.get("secret");

        final Optional<HClientAccessKeys> credentials;
        if (accessKey != null && secret != null) {
          credentials = Optional.of(
            new HClientAccessKeys(accessKey, secret)
          );
        } else {
          credentials = Optional.empty();
        }

        yield new AzStoreS3(
          new AzStoreID(x.get(STORES.STORE_ID)),
          x.get(STORES.STORE_TITLE),
          region,
          endpoint,
          credentials
        );
      }
      default -> {
        throw new DDatabaseException(
          "Unrecognized store type: %s".formatted(type),
          "error-database-data",
          this.attributes(),
          Optional.empty()
        );
      }
    };
  }
}
