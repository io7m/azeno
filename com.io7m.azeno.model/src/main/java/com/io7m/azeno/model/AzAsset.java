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


package com.io7m.azeno.model;

import java.util.Objects;

/**
 * An asset.
 *
 * @param id         The id
 * @param collection The collection to which the asset belongs
 * @param properties The properties
 * @param hash       The asset hash value
 */

public record AzAsset(
  AzAssetID id,
  AzCollectionID collection,
  AzHashType hash,
  AzAssetProperties properties)
  implements AzAsset1NType
{
  /**
   * An asset.
   *
   * @param id         The id
   * @param collection The collection to which the asset belongs
   * @param properties The properties
   * @param hash       The asset hash value
   */

  public AzAsset
  {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(collection, "collection");
    Objects.requireNonNull(properties, "properties");
    Objects.requireNonNull(hash, "hash");
  }

  /**
   * @return A summary of this asset
   */

  public AzAssetSummary summary()
  {
    return new AzAssetSummary(this.id, this.collection);
  }
}
