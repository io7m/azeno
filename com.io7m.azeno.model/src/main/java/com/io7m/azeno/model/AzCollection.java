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
 * A collection.
 *
 * @param id     The collection ID
 * @param title  The collection title
 * @param store  The store to which the collection belongs
 * @param schema The schema used for the collection
 */

public record AzCollection(
  AzCollectionID id,
  String title,
  AzStoreID store,
  AzSchemaID schema)
{
  /**
   * A collection.
   *
   * @param id     The collection ID
   * @param title  The collection title
   * @param store  The store to which the collection belongs
   * @param schema The schema used for the collection
   */

  public AzCollection
  {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(store, "store");
    Objects.requireNonNull(schema, "schema");
  }
}
