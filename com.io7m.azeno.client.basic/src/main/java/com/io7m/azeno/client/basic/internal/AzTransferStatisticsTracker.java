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

import com.io7m.azeno.client.api.AzClientTransferStatistics;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.lang.Integer.toUnsignedString;

/**
 * A tracker for transfer statistics.
 */

final class AzTransferStatisticsTracker implements AutoCloseable
{
  private final long expected;
  private final AzOctetsPerSecond octetsPerSecond;
  private final Consumer<AzClientTransferStatistics> consumer;
  private final ScheduledExecutorService executor;
  private volatile long transferred;

  /**
   * Create a new tracker and start delivering statistics to the given consumer.
   *
   * @param clock      The clock
   * @param inExpected The expected transfer size
   * @param inConsumer The statistics receiver
   */

  AzTransferStatisticsTracker(
    final Clock clock,
    final long inExpected,
    final Consumer<AzClientTransferStatistics> inConsumer)
  {
    this.expected =
      inExpected;
    this.octetsPerSecond =
      new AzOctetsPerSecond(clock);
    this.consumer =
      Objects.requireNonNull(inConsumer, "consumer");
    this.executor =
      Executors.newSingleThreadScheduledExecutor(r -> {
        return Thread.ofVirtual().unstarted(r);
      });

    this.executor.scheduleAtFixedRate(
      this::broadcast,
      0L,
      1L,
      TimeUnit.SECONDS
    );
  }

  /**
   * The given number of octets have been transferred.
   *
   * @param octets The octet count
   */

  void add(
    final long octets)
  {
    this.transferred += octets;
    if (this.octetsPerSecond.isReadyForMore()) {
      this.octetsPerSecond.add(octets);
    }
  }

  /**
   * Broadcast state now.
   */

  void broadcast()
  {
    this.consumer.accept(this.sample());
  }

  /**
   * @return A sample of the current statistics
   */

  AzClientTransferStatistics sample()
  {
    return new AzClientTransferStatistics(
      this.expected,
      this.transferred,
      this.octetsPerSecond.average()
    );
  }

  @Override
  public void close()
    throws Exception
  {
    this.executor.shutdown();
    this.executor.awaitTermination(30L, TimeUnit.SECONDS);
  }

  @Override
  public String toString()
  {
    return "[AzTransferStatisticsTracker 0x%s]"
      .formatted(toUnsignedString(this.hashCode(), 16));
  }

  /**
   * The transfer is completed.
   */

  public void completed()
  {
    this.transferred = this.expected;
    this.broadcast();
  }
}
