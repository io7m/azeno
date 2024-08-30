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

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * An inclusive range of time values.
 *
 * @param lower The lower bound
 * @param upper The upper bound
 */

public record AzTimeRange(
  OffsetDateTime lower,
  OffsetDateTime upper)
{
  /**
   * An inclusive range of time values.
   *
   * @param lower The lower bound
   * @param upper The upper bound
   */

  public AzTimeRange
  {
    Objects.requireNonNull(lower, "lower");
    Objects.requireNonNull(upper, "upper");

    if (lower.isAfter(upper)) {
      throw new AzValidityException("Upper time must be <= lower time.");
    }
  }
}
