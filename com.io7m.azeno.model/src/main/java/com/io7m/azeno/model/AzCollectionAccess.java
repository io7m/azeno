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
 * A value representing access to a collection.
 *
 * @param user       The user
 * @param collection The collection
 * @param read       {@code true} if reading is permitted
 * @param write      {@code true} if writing is permitted
 */

public record AzCollectionAccess(
  AzUserID user,
  AzCollectionID collection,
  boolean read,
  boolean write)
{
  /**
   * A value representing access to a collection.
   *
   * @param user       The user
   * @param collection The collection
   * @param read       {@code true} if reading is permitted
   * @param write      {@code true} if writing is permitted
   */

  public AzCollectionAccess
  {
    Objects.requireNonNull(user, "user");
    Objects.requireNonNull(collection, "collection");
  }

  /**
   * @return This access value as a request
   */

  public AzCollectionAccessRequest request()
  {
    return new AzCollectionAccessRequest(
      this.user,
      this.collection
    );
  }
}
