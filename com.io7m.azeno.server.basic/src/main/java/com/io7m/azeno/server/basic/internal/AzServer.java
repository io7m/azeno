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

package com.io7m.azeno.server.basic.internal;

import com.io7m.azeno.database.api.AzDatabaseConfiguration;
import com.io7m.azeno.database.api.AzDatabaseType;
import com.io7m.azeno.database.api.AzUserPutType;
import com.io7m.azeno.error_codes.AzErrorCode;
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.protocol.asset.cb.AzA1Messages;
import com.io7m.azeno.security.AzSecurity;
import com.io7m.azeno.security.AzSecurityPolicy;
import com.io7m.azeno.server.api.AzServerConfiguration;
import com.io7m.azeno.server.api.AzServerException;
import com.io7m.azeno.server.api.AzServerType;
import com.io7m.azeno.server.asset.v1.AzA1Server;
import com.io7m.azeno.server.service.clock.AzServerClock;
import com.io7m.azeno.server.service.configuration.AzConfigurationService;
import com.io7m.azeno.server.service.configuration.AzConfigurationServiceType;
import com.io7m.azeno.server.service.health.AzServerHealth;
import com.io7m.azeno.server.service.idstore.AzIdstoreClients;
import com.io7m.azeno.server.service.idstore.AzIdstoreClientsType;
import com.io7m.azeno.server.service.maintenance.AzMaintenanceService;
import com.io7m.azeno.server.service.reqlimit.AzRequestLimits;
import com.io7m.azeno.server.service.sessions.AzSessionService;
import com.io7m.azeno.server.service.telemetry.api.AzMetricsService;
import com.io7m.azeno.server.service.telemetry.api.AzMetricsServiceType;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryNoOp;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceFactoryType;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType;
import com.io7m.azeno.server.service.tls.AzTLSContextService;
import com.io7m.azeno.server.service.tls.AzTLSContextServiceType;
import com.io7m.azeno.server.service.verdant.AzVerdantMessages;
import com.io7m.azeno.server.service.verdant.AzVerdantMessagesType;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.idstore.model.IdName;
import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.medrina.api.MSubject;
import com.io7m.repetoir.core.RPServiceDirectory;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.io7m.azeno.strings.AzStringConstants.ERROR_REQUEST_TOO_LARGE;
import static java.lang.Integer.toUnsignedString;

/**
 * The basic server frontend.
 */

public final class AzServer implements AzServerType
{
  private final AzServerConfiguration configuration;
  private final AtomicBoolean stopped;
  private CloseableCollectionType<AzServerException> resources;
  private AzServerTelemetryServiceType telemetry;
  private AzDatabaseType database;

  /**
   * The basic server frontend.
   *
   * @param inConfiguration The server configuration
   */

  public AzServer(
    final AzServerConfiguration inConfiguration)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.resources =
      createResourceCollection();
    this.stopped =
      new AtomicBoolean(true);
  }

  private static CloseableCollectionType<AzServerException> createResourceCollection()
  {
    return CloseableCollection.create(
      () -> {
        return new AzServerException(
          "Server creation failed.",
          new AzErrorCode("server-creation"),
          Map.of(),
          Optional.empty()
        );
      }
    );
  }

  @Override
  public void start()
    throws AzServerException
  {
    try {
      if (this.stopped.compareAndSet(true, false)) {
        this.resources = createResourceCollection();
        this.telemetry = this.createTelemetry();

        final var startupSpan =
          this.telemetry.tracer()
            .spanBuilder("AzServer.start")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan();

        try {
          this.database =
            this.resources.add(this.createDatabase());

          final var services =
            this.resources.add(this.createServiceDirectory(this.database));

          final var asset = AzA1Server.create(services);
          this.resources.add(asset::stop);

          /*
           * After everything is started, load the security policy. The default
           * policy is deny-all, so this effectively opens the server for access.
           */

          AzSecurity.setPolicy(AzSecurityPolicy.open());
        } catch (final DDatabaseException e) {
          startupSpan.recordException(e);

          try {
            this.close();
          } catch (final AzServerException ex) {
            e.addSuppressed(ex);
          }
          throw new AzServerException(
            e.getMessage(),
            e,
            new AzErrorCode("database"),
            e.attributes(),
            e.remediatingAction()
          );
        } catch (final Exception e) {
          startupSpan.recordException(e);

          try {
            this.close();
          } catch (final AzServerException ex) {
            e.addSuppressed(ex);
          }
          throw new AzServerException(
            e.getMessage(),
            e,
            new AzErrorCode("startup"),
            Map.of(),
            Optional.empty()
          );
        } finally {
          startupSpan.end();
        }
      }
    } catch (final Throwable e) {
      this.close();
      throw e;
    }
  }

  private RPServiceDirectoryType createServiceDirectory(
    final AzDatabaseType newDatabase)
  {
    final var services = new RPServiceDirectory();
    final var strings = this.configuration.strings();
    services.register(AzStrings.class, strings);

    services.register(AzServerTelemetryServiceType.class, this.telemetry);
    services.register(AzDatabaseType.class, newDatabase);

    final var verdant = new AzVerdantMessages();
    services.register(AzVerdantMessagesType.class, verdant);

    final var metrics = new AzMetricsService(this.telemetry);
    services.register(AzMetricsServiceType.class, metrics);

    final var health = AzServerHealth.create(services);
    services.register(AzServerHealth.class, health);

    final var sessionAssetService =
      new AzSessionService(
        metrics,
        this.configuration.assetApiConfiguration()
          .sessionExpiration()
          .orElseGet(() -> Duration.ofDays(3650L))
      );

    services.register(AzSessionService.class, sessionAssetService);

    final var idstore =
      AzIdstoreClients.create(
        this.configuration.locale(),
        this.configuration.clock(),
        this.telemetry,
        this.configuration.idstoreConfiguration()
      );
    services.register(AzIdstoreClientsType.class, idstore);

    final var configService = new AzConfigurationService(this.configuration);
    services.register(AzConfigurationServiceType.class, configService);

    final var clock = new AzServerClock(this.configuration.clock());
    services.register(AzServerClock.class, clock);

    final var vMessages = new AzVerdantMessages();
    services.register(AzVerdantMessagesType.class, vMessages);

    final var idA1Messages = new AzA1Messages();
    services.register(AzA1Messages.class, idA1Messages);

    final var tls = AzTLSContextService.createService(services);
    services.register(AzTLSContextServiceType.class, tls);

    final var maintenance =
      AzMaintenanceService.create(
        clock,
        this.telemetry,
        configService,
        tls,
        this.database
      );
    services.register(AzMaintenanceService.class, maintenance);

    services.register(
      AzRequestLimits.class,
      new AzRequestLimits(configService, (final Long size) -> {
        return strings.format(ERROR_REQUEST_TOO_LARGE, size);
      }));
    return services;
  }

  private AzDatabaseType createDatabase()
    throws DDatabaseException
  {
    final var databaseConfigurationInfo =
      this.configuration.databaseConfiguration();

    final var dbConfiguration =
      new AzDatabaseConfiguration(
        this.configuration.strings(),
        this.telemetry,
        databaseConfigurationInfo
      );

    return this.configuration.databases()
      .open(dbConfiguration, event -> {

      });
  }

  private AzServerTelemetryServiceType createTelemetry()
  {
    return this.configuration.openTelemetry()
      .flatMap(config -> {
        final var loader =
          ServiceLoader.load(AzServerTelemetryServiceFactoryType.class);
        return loader.findFirst().map(f -> f.create(config));
      }).orElseGet(AzServerTelemetryNoOp::noop);
  }

  @Override
  public AzDatabaseType database()
  {
    if (this.stopped.get()) {
      throw new IllegalStateException("Server is not started.");
    }

    return this.database;
  }

  @Override
  public AzServerConfiguration configuration()
  {
    return this.configuration;
  }

  @Override
  public boolean isClosed()
  {
    return this.stopped.get();
  }

  @Override
  public void setUserAsAdmin(
    final AzUserID adminId,
    final IdName adminName)
    throws AzServerException
  {
    Objects.requireNonNull(adminId, "adminId");
    Objects.requireNonNull(adminName, "adminName");

    final var newTelemetry =
      this.createTelemetry();

    final var dbConfigurationInfo =
      this.configuration.databaseConfiguration()
        .withoutUpgradeOrCreate();

    final var dbConfiguration =
      new AzDatabaseConfiguration(
        this.configuration.strings(),
        newTelemetry,
        dbConfigurationInfo
      );

    try (var newDatabase =
           this.configuration.databases()
             .open(dbConfiguration, event -> {

             })) {

      final var span =
        newTelemetry.tracer()
          .spanBuilder("SetUserAsAdmin")
          .startSpan();

      try (var ignored = span.makeCurrent()) {
        setUserAsAdminSpan(newDatabase, adminId, adminName);
      } catch (final Exception e) {
        span.recordException(e);
        span.setStatus(StatusCode.ERROR);
        throw e;
      } finally {
        span.end();
      }
    } catch (final DDatabaseException e) {
      throw new AzServerException(
        e.getMessage(),
        e,
        new AzErrorCode(e.errorCode()),
        e.attributes(),
        e.remediatingAction()
      );
    }
  }

  private static void setUserAsAdminSpan(
    final AzDatabaseType database,
    final AzUserID adminId,
    final IdName adminName)
    throws AzServerException
  {
    try (var connection =
           database.openConnection()) {
      try (var transaction =
             connection.openTransaction()) {
        final var put =
          transaction.query(AzUserPutType.class);

        transaction.setUserID(adminId);
        put.execute(
          new AzUser(
            adminId,
            adminName,
            new MSubject(AzSecurityPolicy.ROLES_ALL)
          )
        );

        transaction.commit();
      }
    } catch (final DDatabaseException e) {
      throw new AzServerException(
        e.getMessage(),
        e,
        new AzErrorCode(e.errorCode()),
        e.attributes(),
        e.remediatingAction()
      );
    }
  }

  @Override
  public void close()
    throws AzServerException
  {
    if (this.stopped.compareAndSet(false, true)) {
      this.resources.close();
    }
  }

  @Override
  public String toString()
  {
    return "[AzServer 0x%s]".formatted(toUnsignedString(this.hashCode(), 16));
  }
}
