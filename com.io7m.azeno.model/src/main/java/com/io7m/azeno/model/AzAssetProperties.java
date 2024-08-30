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

import com.io7m.lanark.core.RDottedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The properties for an asset.
 *
 * @param values The property values
 */

public record AzAssetProperties(
  Map<RDottedName, List<AzValueType>> values)
{
  /**
   * The properties for an asset.
   *
   * @param values The property values
   */

  public AzAssetProperties
  {
    values = Map.copyOf(values);
  }

  /**
   * @return A mutable asset builder
   */

  public static Builder builder()
  {
    return new Builder();
  }

  /**
   * A mutable asset builder.
   */

  public static final class Builder
  {
    private final HashMap<RDottedName, List<AzValueType>> buildValues;

    private Builder()
    {
      this.buildValues = new HashMap<>();
    }

    /**
     * Add a value.
     *
     * @param value The value
     *
     * @return this
     */

    public Builder put(
      final AzValueType value)
    {
      Objects.requireNonNull(value, "value");

      List<AzValueType> existing = this.buildValues.get(value.name());
      if (existing == null) {
        existing = new ArrayList<>();
      }

      existing.add(value);
      this.buildValues.put(value.name(), existing);
      return this;
    }

    /**
     * Build the asset properties.
     *
     * @return The properties
     */

    public AzAssetProperties build()
    {
      return new AzAssetProperties(Map.copyOf(this.buildValues));
    }
  }
}
