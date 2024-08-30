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


package com.io7m.azeno.database.api;

import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DDatabaseCreate;
import com.io7m.darco.api.DDatabaseTelemetryType;
import com.io7m.darco.api.DDatabaseUpgrade;
import com.io7m.darco.api.DUsernamePassword;
import com.io7m.darco.postgres.DPQDatabaseConfigurationType;

import java.util.Objects;

/**
 * The database configuration.
 *
 * @param telemetry The telemetry
 * @param strings   The string resources
 * @param info      The info
 */

public record AzDatabaseConfiguration(
  AzStrings strings,
  DDatabaseTelemetryType telemetry,
  AzDatabaseConfigurationInfo info)
  implements DPQDatabaseConfigurationType
{
  /**
   * The database configuration.
   *
   * @param telemetry The telemetry
   * @param strings   The string resources
   * @param info      The info
   */

  public AzDatabaseConfiguration
  {
    Objects.requireNonNull(telemetry, "telemetry");
    Objects.requireNonNull(strings, "strings");
    Objects.requireNonNull(info, "info");
  }

  @Override
  public DUsernamePassword workerRole()
  {
    return new DUsernamePassword("azeno", this.info.workerRolePassword());
  }

  /**
   * @return This configuration without creation or upgrades
   */

  public AzDatabaseConfiguration withoutUpgradeOrCreate()
  {
    return new AzDatabaseConfiguration(
      this.strings,
      this.telemetry,
      new AzDatabaseConfigurationInfo(
        DDatabaseCreate.DO_NOT_CREATE_DATABASE,
        DDatabaseUpgrade.DO_NOT_UPGRADE_DATABASE,
        this.databaseAddress(),
        this.databasePort(),
        this.databaseName(),
        this.databaseUseTLS(),
        this.ownerRole(),
        this.info.workerRolePassword(),
        this.info.readerRolePassword())
    );
  }

  @Override
  public String databaseAddress()
  {
    return this.info.databaseAddress();
  }

  @Override
  public String databaseName()
  {
    return this.info.databaseName();
  }

  @Override
  public int databasePort()
  {
    return this.info.databasePort();
  }

  @Override
  public boolean databaseUseTLS()
  {
    return this.info.databaseUseTLS();
  }

  @Override
  public DUsernamePassword ownerRole()
  {
    return this.info.ownerRole();
  }

  @Override
  public DDatabaseCreate create()
  {
    return this.info.create();
  }

  @Override
  public DDatabaseUpgrade upgrade()
  {
    return this.info.upgrade();
  }
}
