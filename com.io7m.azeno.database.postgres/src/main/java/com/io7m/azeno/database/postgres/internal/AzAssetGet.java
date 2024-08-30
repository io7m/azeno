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

import com.io7m.anethum.api.ParsingException;
import com.io7m.azeno.database.api.AzAssetGetType;
import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.model.AzAsset;
import com.io7m.azeno.model.AzAssetID;
import com.io7m.azeno.model.AzAssetProperties;
import com.io7m.azeno.model.AzAssets;
import com.io7m.azeno.model.AzCollectionID;
import com.io7m.azeno.model.AzHashSHA256;
import com.io7m.azeno.model.AzHashType;
import com.io7m.darco.api.DDatabaseException;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Optional;

import static com.io7m.azeno.database.postgres.internal.Tables.ASSETS;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorIo;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * AssetGet.
 */

public final class AzAssetGet
  extends AzDatabaseQueryAbstract<AzAssetID, Optional<AzAsset>>
  implements AzAssetGetType
{
  AzAssetGet(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  /**
   * @return The query provider
   */

  public static AzDatabaseQueryProviderType<AzAssetID, Optional<AzAsset>, AzAssetGetType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzAssetGetType.class,
      AzAssetGet::new
    );
  }

  @Override
  protected Optional<AzAsset> onExecute(
    final AzDatabaseTransactionType transaction,
    final AzAssetID id)
    throws DDatabaseException
  {
    this.putAttribute("AssetID", id);

    final var context =
      transaction.get(DSLContext.class);

    final var r =
      context.select(
          ASSETS.ASSET_STATE,
          ASSETS.ASSET_ID,
          ASSETS.ASSET_COLLECTION,
          ASSETS.ASSET_HASH_VALUE,
          ASSETS.ASSET_HASH_ALGORITHM,
          ASSETS.ASSET_DATA_TYPE,
          ASSETS.ASSET_DATA)
        .from(ASSETS)
        .where(ASSETS.ASSET_ID.eq(id.id()))
        .fetchOptional();

    if (r.isEmpty()) {
      return Optional.empty();
    }

    try {
      return Optional.of(mapRecord(r.get()));
    } catch (final ParsingException e) {
      throw new DDatabaseException(
        e.getMessage(),
        e,
        errorIo().id(),
        this.attributes(),
        Optional.empty()
      );
    }
  }

  private static AzAsset mapRecord(
    final Record x)
    throws ParsingException
  {
    return new AzAsset(
      new AzAssetID(x.get(ASSETS.ASSET_ID)),
      new AzCollectionID(x.get(ASSETS.ASSET_COLLECTION)),
      hashOf(
        x.get(ASSETS.ASSET_HASH_ALGORITHM),
        x.get(ASSETS.ASSET_HASH_VALUE)),
      propertiesOf(
        x.get(ASSETS.ASSET_DATA_TYPE),
        x.get(ASSETS.ASSET_DATA)
      )
    );
  }

  private static AzAssetProperties propertiesOf(
    final String assetDataType,
    final String assetData)
    throws ParsingException
  {
    final var parser =
      AzXML.assetParsers();

    final var assetR =
      parser.parse(
        URI.create("urn:db"),
        new ByteArrayInputStream(assetData.getBytes(UTF_8))
      );

    return switch (assetR) {
      case final AzAsset azAsset -> {
        yield azAsset.properties();
      }
      case final AzAssets ignored -> {
        throw new IllegalStateException(
          "Cannot store multiple assets in a column.");
      }
    };
  }

  private static AzHashType hashOf(
    final String algorithm,
    final String value)
  {
    return switch (algorithm) {
      case "SHA-256" -> {
        yield new AzHashSHA256(value);
      }
      default -> {
        throw new IllegalStateException(
          "Unrecognized hash algorithm: %s".formatted(algorithm)
        );
      }
    };
  }
}
