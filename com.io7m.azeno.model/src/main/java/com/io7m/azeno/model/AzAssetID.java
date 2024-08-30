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
import java.util.UUID;

/**
 * The unique ID of an asset.
 *
 * @param id The ID value
 */

public record AzAssetID(UUID id)
  implements Comparable<AzAssetID>
{
  /**
   * The unique ID of an asset.
   *
   * @param id The ID value
   */

  public AzAssetID
  {
    Objects.requireNonNull(id, "id");
  }

  /**
   * Construct an ID.
   *
   * @param id The ID value
   *
   * @return An ID
   */

  public static AzAssetID of(
    final String id)
  {
    return new AzAssetID(UUID.fromString(id));
  }

  /**
   * Construct an ID.
   *
   * @param id The ID value
   *
   * @return An ID
   */

  public static AzAssetID of(
    final UUID id)
  {
    return new AzAssetID(id);
  }

  /**
   * Construct an ID using a pseudorandom value.
   *
   * @return An ID
   */

  public static AzAssetID random()
  {
    return new AzAssetID(UUID.randomUUID());
  }

  @Override
  public String toString()
  {
    return this.id.toString();
  }

  @Override
  public int compareTo(
    final AzAssetID other)
  {
    return this.id.compareTo(other.id);
  }
}
