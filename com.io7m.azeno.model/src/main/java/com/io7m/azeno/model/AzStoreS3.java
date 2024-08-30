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

import com.io7m.huanuco.api.HClientAccessKeys;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * A store backed by S3-compatible storage.
 *
 * @param id          The ID
 * @param title       The store title
 * @param region      The S3 region (such as "us-east-1")
 * @param endpoint    The endpoint
 * @param credentials The credentials if any are required
 */

public record AzStoreS3(
  AzStoreID id,
  String title,
  String region,
  URI endpoint,
  Optional<HClientAccessKeys> credentials)
  implements AzStoreType
{
  /**
   * A store backed by S3-compatible storage.
   *
   * @param id          The ID
   * @param title       The store title
   * @param region      The S3 region (such as "us-east-1")
   * @param endpoint    The endpoint
   * @param credentials The credentials if any are required
   */

  public AzStoreS3
  {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(region, "region");
    Objects.requireNonNull(endpoint, "endpoint");
    Objects.requireNonNull(credentials, "credentials");
  }

  @Override
  public AzStoreKind kind()
  {
    return AzStoreKind.S3;
  }

  @Override
  public AzStoreSummary summary()
  {
    return new AzStoreSummary(this.id, this.title);
  }
}
