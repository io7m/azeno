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


package com.io7m.azeno.server.service.maintenance;

import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.database.api.AzDatabaseType;
import com.io7m.azeno.server.service.clock.AzServerClock;
import com.io7m.azeno.server.service.configuration.AzConfigurationServiceType;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType;
import com.io7m.azeno.server.service.tls.AzTLSContextServiceType;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.repetoir.core.RPServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A service that performs nightly database maintenance.
 */

public final class AzMaintenanceService
  implements RPServiceType, AutoCloseable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AzMaintenanceService.class);

  private final ExecutorService executor;
  private final AzServerClock clock;
  private final AzServerTelemetryServiceType telemetry;
  private final AzDatabaseType database;
  private final AzTLSContextServiceType tlsContexts;
  private final AzConfigurationServiceType configuration;
  private final AtomicBoolean closed;
  private final CompletableFuture<Void> waitTLS;
  private final CompletableFuture<Void> waitMaintenance;

  private AzMaintenanceService(
    final ExecutorService inExecutor,
    final AzServerClock inClock,
    final AzServerTelemetryServiceType inTelemetry,
    final AzDatabaseType inDatabase,
    final AzTLSContextServiceType inTlsContexts,
    final AzConfigurationServiceType inConfiguration)
  {
    this.executor =
      Objects.requireNonNull(inExecutor, "executor");
    this.clock =
      Objects.requireNonNull(inClock, "clock");
    this.telemetry =
      Objects.requireNonNull(inTelemetry, "telemetry");
    this.database =
      Objects.requireNonNull(inDatabase, "database");
    this.tlsContexts =
      Objects.requireNonNull(inTlsContexts, "tlsContexts");
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.closed =
      new AtomicBoolean(false);
    this.waitTLS =
      new CompletableFuture<Void>();
    this.waitMaintenance =
      new CompletableFuture<Void>();
  }

  /**
   * A service that performs nightly maintenance.
   *
   * @param clock         The clock
   * @param telemetry     The telemetry service
   * @param database      The database
   * @param configuration The configuration service
   * @param tlsContexts   The TLS contexts
   *
   * @return The service
   */

  public static AzMaintenanceService create(
    final AzServerClock clock,
    final AzServerTelemetryServiceType telemetry,
    final AzConfigurationServiceType configuration,
    final AzTLSContextServiceType tlsContexts,
    final AzDatabaseType database)
  {
    Objects.requireNonNull(clock, "clock");
    Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(database, "database");
    Objects.requireNonNull(telemetry, "telemetry");
    Objects.requireNonNull(tlsContexts, "tlsContexts");

    final var executor =
      Executors.newThreadPerTaskExecutor(
        Thread.ofVirtual()
          .name("com.io7m.azeno.maintenance-", 0L)
          .factory()
      );

    final var maintenanceService =
      new AzMaintenanceService(
        executor,
        clock,
        telemetry,
        database,
        tlsContexts,
        configuration
      );

    executor.execute(maintenanceService::runTLSReloadTask);
    executor.execute(maintenanceService::runMaintenanceTask);
    return maintenanceService;
  }

  /**
   * A task that executes maintenance once when the service starts, and then
   * again at every subsequent midnight.
   */

  private void runMaintenanceTask()
  {
    while (!this.closed.get()) {
      try {
        this.runMaintenance();
      } catch (final Exception e) {
        // Not important.
      }

      final var timeNow =
        this.clock.now();
      final var timeNextMidnight =
        timeNow.withHour(0)
          .withMinute(0)
          .withSecond(0)
          .plusDays(1L);

      final var untilNext =
        Duration.between(timeNow, timeNextMidnight);

      try {
        this.waitMaintenance.get(untilNext.toSeconds(), TimeUnit.SECONDS);
      } catch (final Exception e) {
        break;
      }
    }
  }

  /**
   * A task that reloads TLS contexts at the specified reload interval.
   */

  private void runTLSReloadTask()
  {
    final var reloadIntervalOpt =
      this.configuration.configuration()
        .maintenanceConfiguration()
        .tlsReloadInterval();

    if (reloadIntervalOpt.isEmpty()) {
      return;
    }

    final var reloadInterval =
      reloadIntervalOpt.get();

    while (!this.closed.get()) {
      try {
        this.runTLSReload();
      } catch (final Exception e) {
        // Not important.
      }

      try {
        this.waitTLS.get(reloadInterval.toSeconds(), TimeUnit.SECONDS);
      } catch (final Exception e) {
        break;
      }
    }
  }

  private void runTLSReload()
  {
    LOG.info("Reloading TLS contexts");
    this.tlsContexts.reload();
  }

  private void runMaintenance()
  {
    LOG.info("Maintenance task starting");

    final var span =
      this.telemetry.tracer()
        .spanBuilder("Maintenance")
        .startSpan();

    try (var ignored = span.makeCurrent()) {
      try (var connection =
             this.database.openConnection()) {
        try (var transaction =
               connection.openTransaction()) {

          try {
            executeDatabaseMaintenance(transaction);
          } catch (final Exception e) {
            span.recordException(e);
          }

          LOG.info("Maintenance task completed.");
        }
      }
    } catch (final Exception e) {
      LOG.error("Maintenance task failed: ", e);
      span.recordException(e);
    } finally {
      span.end();
    }
  }

  private static void executeDatabaseMaintenance(
    final AzDatabaseTransactionType transaction)
    throws DDatabaseException
  {

  }

  @Override
  public String description()
  {
    return "Server maintenance service.";
  }

  @Override
  public void close()
  {
    if (this.closed.compareAndSet(false, true)) {
      this.waitTLS.complete(null);
      this.waitMaintenance.complete(null);
      this.executor.close();
    }
  }

  @Override
  public String toString()
  {
    return "[AzMaintenanceService 0x%s]"
      .formatted(Long.toUnsignedString(this.hashCode(), 16));
  }
}
