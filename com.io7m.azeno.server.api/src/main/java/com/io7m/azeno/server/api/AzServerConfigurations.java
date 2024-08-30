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

import com.io7m.azeno.database.api.AzDatabaseConfigurationInfo;
import com.io7m.azeno.database.api.AzDatabaseFactoryType;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DUsernamePassword;
import com.io7m.lanark.core.RDottedName;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import static com.io7m.darco.api.DDatabaseCreate.CREATE_DATABASE;
import static com.io7m.darco.api.DDatabaseCreate.DO_NOT_CREATE_DATABASE;
import static com.io7m.darco.api.DDatabaseUpgrade.DO_NOT_UPGRADE_DATABASE;
import static com.io7m.darco.api.DDatabaseUpgrade.UPGRADE_DATABASE;

/**
 * Functions to produce server configurations.
 */

public final class AzServerConfigurations
{
  private AzServerConfigurations()
  {

  }

  /**
   * Read a server configuration from the given file.
   *
   * @param locale      The locale
   * @param clock       The clock
   * @param file        The file
   *
   * @return A server configuration
   */

  public static AzServerConfiguration ofFile(
    final Locale locale,
    final Clock clock,
    final AzServerConfigurationFile file)
  {
    Objects.requireNonNull(locale, "locale");
    Objects.requireNonNull(clock, "clock");
    Objects.requireNonNull(file, "file");

    final var strings =
      AzStrings.create(locale);

    final var fileDbConfig =
      file.databaseConfiguration();

    final var databaseConfiguration =
      new AzDatabaseConfigurationInfo(
        fileDbConfig.create() ? CREATE_DATABASE : DO_NOT_CREATE_DATABASE,
        fileDbConfig.upgrade() ? UPGRADE_DATABASE : DO_NOT_UPGRADE_DATABASE,
        fileDbConfig.address(),
        fileDbConfig.port(),
        fileDbConfig.databaseName(),
        false,
        new DUsernamePassword(
          fileDbConfig.ownerRoleName(),
          fileDbConfig.ownerRolePassword()
        ),
        fileDbConfig.workerRolePassword(),
        fileDbConfig.readerRolePassword()
      );

    final var databaseFactories =
      ServiceLoader.load(AzDatabaseFactoryType.class)
        .iterator();

    final var database =
      findDatabase(databaseFactories, fileDbConfig.kind());

    return new AzServerConfiguration(
      locale,
      clock,
      strings,
      database,
      databaseConfiguration,
      file.assetService(),
      file.idstoreConfiguration(),
      file.limitsConfiguration(),
      file.maintenanceConfiguration(),
      file.openTelemetry()
    );
  }

  private static AzDatabaseFactoryType findDatabase(
    final Iterator<AzDatabaseFactoryType> databaseFactories,
    final AzServerDatabaseKind kind)
  {
    if (!databaseFactories.hasNext()) {
      throw new ServiceConfigurationError(
        "No available implementations of type %s"
          .formatted(AzDatabaseFactoryType.class)
      );
    }

    final var kinds = new ArrayList<RDottedName>();
    while (databaseFactories.hasNext()) {
      final var database = databaseFactories.next();
      kinds.add(database.kind());
      if (Objects.equals(database.kind(), kind.dottedName())) {
        return database;
      }
    }

    throw new ServiceConfigurationError(
      "No available databases of kind %s (Available databases include: %s)"
        .formatted(AzDatabaseFactoryType.class, kinds)
    );
  }
}
