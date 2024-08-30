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


package com.io7m.azeno.server.service.configuration.v1;

import com.io7m.azeno.server.api.AzServerConfigurationFile;
import com.io7m.azeno.server.api.AzServerDatabaseConfiguration;
import com.io7m.azeno.server.api.AzServerHTTPServiceConfiguration;
import com.io7m.azeno.server.api.AzServerIdstoreConfiguration;
import com.io7m.azeno.server.api.AzServerLimitsConfiguration;
import com.io7m.azeno.server.api.AzServerMaintenanceConfiguration;
import com.io7m.azeno.server.api.AzServerOpenTelemetryConfiguration;
import com.io7m.blackthorne.core.BTElementHandlerConstructorType;
import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;
import com.io7m.blackthorne.core.BTQualifiedName;

import java.util.Map;
import java.util.Optional;

import static com.io7m.azeno.server.service.configuration.v1.AzC1Names.qName;
import static java.util.Map.entry;

/**
 * The root configuration parser.
 */

public final class AzC1Configuration
  implements BTElementHandlerType<Object, AzServerConfigurationFile>
{
  private AzServerDatabaseConfiguration database;
  private Optional<AzServerOpenTelemetryConfiguration> telemetry;
  private AzServerIdstoreConfiguration idstore;
  private AzServerLimitsConfiguration limits;
  private AzServerHTTPServiceConfiguration asset;
  private AzServerMaintenanceConfiguration maintenance;

  /**
   * The root configuration parser.
   *
   * @param context The context
   */

  public AzC1Configuration(
    final BTElementParsingContextType context)
  {
    this.telemetry = Optional.empty();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      entry(qName("Database"), AzC1Database::new),
      entry(qName("AssetService"), AzC1AssetService::new),
      entry(qName("Idstore"), AzC1Idstore::new),
      entry(qName("Limits"), AzC1Limits::new),
      entry(qName("Maintenance"), AzC1Maintenance::new),
      entry(qName("OpenTelemetry"), AzC1Telemetry::new)
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    switch (result) {
      case final AzServerDatabaseConfiguration c -> {
        this.database = c;
      }
      case final AzServerOpenTelemetryConfiguration c -> {
        this.telemetry = Optional.of(c);
      }
      case final AzServerLimitsConfiguration c -> {
        this.limits = c;
      }
      case final AzServerIdstoreConfiguration c -> {
        this.idstore = c;
      }
      case final AzServerHTTPServiceConfiguration c -> {
        this.asset = c;
      }
      case final AzServerMaintenanceConfiguration c -> {
        this.maintenance = c;
      }
      default -> {
        throw new IllegalArgumentException(
          "Unrecognized element: %s".formatted(result)
        );
      }
    }
  }

  @Override
  public AzServerConfigurationFile onElementFinished(
    final BTElementParsingContextType context)
  {
    return new AzServerConfigurationFile(
      this.asset,
      this.database,
      this.idstore,
      this.limits,
      this.maintenance,
      this.telemetry
    );
  }
}
