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

package com.io7m.azeno.client.basic.internal;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * An octets-per-second calculator.
 */

public final class AzOctetsPerSecond
{
  private final Clock clock;
  private final DescriptiveStatistics stats;
  private volatile boolean first;
  private volatile Instant timeThen;

  /**
   * Create a new octets-per-second service.
   *
   * @param inClock The clock
   */

  public AzOctetsPerSecond(
    final Clock inClock)
  {
    this.clock =
      Objects.requireNonNull(inClock, "clock");
    this.stats =
      new DescriptiveStatistics();
    this.timeThen =
      this.now();

    this.stats.setWindowSize(10);
  }

  /**
   * @return {@code true} if the service is ready for more samples
   */

  public boolean isReadyForMore()
  {
    if (this.secondElapsed() || this.first) {
      this.first = false;
      this.timeThen = this.now();
      return true;
    }
    return false;
  }

  private Instant now()
  {
    return Instant.now(this.clock);
  }

  private boolean secondElapsed()
  {
    return this.now().isAfter(this.timeThen.plusSeconds(1L));
  }

  /**
   * @return The current average
   */

  public double average()
  {
    final var r = this.stats.getMean();
    if (Double.isNaN(r)) {
      return 0.0;
    }
    return r;
  }

  /**
   * Add a new sample
   *
   * @param octets The number of octets received
   */

  public void add(
    final long octets)
  {
    this.stats.addValue((double) octets);
  }

  @Override
  public String toString()
  {
    return String.format(
      "[CAOctetsPerSecond 0x%08x]",
      Integer.valueOf(this.hashCode())
    );
  }
}
