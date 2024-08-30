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

import com.io7m.darco.api.DDatabaseCreate;
import com.io7m.darco.api.DDatabaseUpgrade;
import com.io7m.darco.api.DUsernamePassword;

import java.util.Objects;
import java.util.Optional;

/**
 * The database configuration.
 *
 * @param create             The database creation flag
 * @param upgrade            The database upgrade flag
 * @param databaseAddress    The database address
 * @param databasePort       The database port
 * @param databaseName       The database name
 * @param databaseUseTLS     Whether to use TLS to connect to the database
 * @param ownerRole          The database owner role
 * @param workerRolePassword The database worker role password
 * @param readerRolePassword The database reader role password
 */

public record AzDatabaseConfigurationInfo(
  DDatabaseCreate create,
  DDatabaseUpgrade upgrade,
  String databaseAddress,
  int databasePort,
  String databaseName,
  boolean databaseUseTLS,
  DUsernamePassword ownerRole,
  String workerRolePassword,
  Optional<String> readerRolePassword)
{
  /**
   * The database configuration.
   *
   * @param create             The database creation flag
   * @param upgrade            The database upgrade flag
   * @param databaseAddress    The database address
   * @param databasePort       The database port
   * @param databaseName       The database name
   * @param databaseUseTLS     Whether to use TLS to connect to the database
   * @param ownerRole          The database owner role
   * @param workerRolePassword The database worker role password
   * @param readerRolePassword The database reader role password
   */

  public AzDatabaseConfigurationInfo
  {
    Objects.requireNonNull(create, "create");
    Objects.requireNonNull(upgrade, "upgrade");
    Objects.requireNonNull(databaseAddress, "databaseAddress");
    Objects.requireNonNull(databaseName, "databaseName");
    Objects.requireNonNull(ownerRole, "ownerRole");
    Objects.requireNonNull(workerRolePassword, "workerRolePassword");
    Objects.requireNonNull(readerRolePassword, "readerRolePassword");
  }

  /**
   * @return This configuration without creation or upgrades
   */

  public AzDatabaseConfigurationInfo withoutUpgradeOrCreate()
  {
    return new AzDatabaseConfigurationInfo(
      DDatabaseCreate.DO_NOT_CREATE_DATABASE,
      DDatabaseUpgrade.DO_NOT_UPGRADE_DATABASE,
      this.databaseAddress,
      this.databasePort,
      this.databaseName,
      this.databaseUseTLS,
      this.ownerRole,
      this.workerRolePassword,
      this.readerRolePassword
    );
  }
}
