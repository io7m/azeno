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

package com.io7m.azeno.server.api;

import java.util.Objects;
import java.util.Optional;

/**
 * The server configuration file.
 *
 * @param assetService         The asset service configuration
 * @param databaseConfiguration    The database configuration
 * @param idstoreConfiguration     The idstore server configuration
 * @param limitsConfiguration      The limits configuration
 * @param maintenanceConfiguration The maintenance configuration
 * @param openTelemetry            The OpenTelemetry configuration
 */

public record AzServerConfigurationFile(
  AzServerHTTPServiceConfiguration assetService,
  AzServerDatabaseConfiguration databaseConfiguration,
  AzServerIdstoreConfiguration idstoreConfiguration,
  AzServerLimitsConfiguration limitsConfiguration,
  AzServerMaintenanceConfiguration maintenanceConfiguration,
  Optional<AzServerOpenTelemetryConfiguration> openTelemetry)
{
  /**
   * The server configuration file.
   *
   * @param assetService         The asset service configuration
   * @param databaseConfiguration    The database configuration
   * @param idstoreConfiguration     The idstore server configuration
   * @param limitsConfiguration      The limits configuration
   * @param maintenanceConfiguration The maintenance configuration
   * @param openTelemetry            The OpenTelemetry configuration
   */

  public AzServerConfigurationFile
  {
    Objects.requireNonNull(assetService, "assetService");
    Objects.requireNonNull(databaseConfiguration, "databaseConfiguration");
    Objects.requireNonNull(idstoreConfiguration, "idstoreConfiguration");
    Objects.requireNonNull(limitsConfiguration, "limitsConfiguration");
    Objects.requireNonNull(
      maintenanceConfiguration,
      "maintenanceConfiguration");
    Objects.requireNonNull(openTelemetry, "openTelemetry");
  }
}
