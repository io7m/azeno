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

import com.io7m.anethum.api.SerializationException;
import com.io7m.azeno.database.api.AzAssetPutType;
import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.database.postgres.internal.enums.AssetStateT;
import com.io7m.azeno.model.AzAsset;
import com.io7m.azeno.model.AzAuditEvent;
import com.io7m.azeno.model.AzUnit;
import com.io7m.darco.api.DDatabaseException;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;

import static com.io7m.azeno.database.postgres.internal.AZDatabaseExceptions.handleDatabaseException;
import static com.io7m.azeno.database.postgres.internal.Tables.ASSETS;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorIo;

/**
 * AssetPut.
 */

public final class AzAssetPut
  extends AzDatabaseQueryAbstract<AzAsset, AzUnit>
  implements AzAssetPutType
{
  AzAssetPut(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  /**
   * @return The query provider
   */

  public static AzDatabaseQueryProviderType<AzAsset, AzUnit, AzAssetPutType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzAssetPutType.class,
      AzAssetPut::new
    );
  }

  @Override
  protected AzUnit onExecute(
    final AzDatabaseTransactionType transaction,
    final AzAsset asset)
    throws DDatabaseException
  {
    this.putAttribute("AssetID", asset.id());

    final var context =
      transaction.get(DSLContext.class);

    try {
      final var dataText =
        serializeAsset(asset);

      context.insertInto(ASSETS)
        .set(ASSETS.ASSET_COLLECTION, asset.collection().id())
        .set(ASSETS.ASSET_HASH_ALGORITHM, asset.hash().name())
        .set(ASSETS.ASSET_HASH_VALUE, asset.hash().value())
        .set(ASSETS.ASSET_ID, asset.id().id())
        .set(ASSETS.ASSET_STATE, AssetStateT.STATE_UPLOADING)
        .set(ASSETS.ASSET_DATA_TYPE, "com.io7m.azeno.xml:1")
        .set(ASSETS.ASSET_DATA, dataText)
        .onDuplicateKeyUpdate()
        .set(ASSETS.ASSET_COLLECTION, asset.collection().id())
        .set(ASSETS.ASSET_HASH_ALGORITHM, asset.hash().name())
        .set(ASSETS.ASSET_HASH_VALUE, asset.hash().value())
        .set(ASSETS.ASSET_ID, asset.id().id())
        .set(ASSETS.ASSET_DATA_TYPE, "com.io7m.azeno.xml:1")
        .set(ASSETS.ASSET_DATA, dataText)
        .execute();

      putAuditEvent(
        context,
        new AzAuditEvent(
          0L,
          OffsetDateTime.now(),
          transaction.userId(),
          "ASSET_UPDATED",
          this.attributes()
        ));

      return AzUnit.UNIT;
    } catch (final DataAccessException e) {
      throw handleDatabaseException(transaction, this.attributes(), e);
    } catch (final SerializationException | IOException e) {
      throw new DDatabaseException(
        e.getMessage(),
        e,
        errorIo().id(),
        this.attributes(),
        Optional.empty()
      );
    }
  }

  private static String serializeAsset(
    final AzAsset asset)
    throws IOException, SerializationException
  {
    try (final var outputStream = new ByteArrayOutputStream()) {
      AzXML.assetSerializers()
        .serialize(
          URI.create("urn:output"),
          outputStream,
          asset
        );
      outputStream.flush();
      return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }
  }
}
