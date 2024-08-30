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


package com.io7m.azeno.tests.containers;

import com.io7m.azeno.database.api.AzDatabaseConfiguration;
import com.io7m.azeno.database.api.AzDatabaseConfigurationInfo;
import com.io7m.azeno.database.api.AzDatabaseType;
import com.io7m.azeno.database.postgres.AzDatabaseFactory;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DDatabaseCreate;
import com.io7m.darco.api.DDatabaseTelemetryNoOp;
import com.io7m.darco.api.DDatabaseUpgrade;
import com.io7m.darco.api.DUsernamePassword;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The basic database fixture.
 *
 * @param postgres      The base database fixture
 * @param configuration The database configuration
 */

public record AzDatabaseFixture(
  AzPostgresFixture postgres,
  AzDatabaseConfiguration configuration)
{
  static final AzDatabaseFactory DATABASES =
    new AzDatabaseFactory();

  private static final String OWNER_ROLE =
    "postgres";

  public static AzDatabaseFixture create(
    final AzPostgresFixture postgres)
    throws IOException, InterruptedException
  {
    final var configurationInfo =
      new AzDatabaseConfigurationInfo(
        DDatabaseCreate.CREATE_DATABASE,
        DDatabaseUpgrade.UPGRADE_DATABASE,
        "::",
        AzFixtures.postgresPort(),
        "azeno",
        false,
        new DUsernamePassword(
          "postgres",
          "12345678"
        ),
        "12345678",
        Optional.of("12345678")
      );

    final var configuration =
      new AzDatabaseConfiguration(
        AzStrings.create(Locale.ROOT),
        DDatabaseTelemetryNoOp.get(),
        configurationInfo
      );

    final var r =
      postgres.container()
        .executeAndWait(
          List.of(
            "createdb",
            "-w",
            "-U",
            postgres.databaseOwner(),
            "azeno"
          ),
          10L,
          TimeUnit.SECONDS
        );

    Assertions.assertEquals(0, r);

    return new AzDatabaseFixture(
      postgres,
      configuration
    );
  }

  /**
   * Create a database from this container and configuration.
   *
   * @return A new database
   *
   * @throws Exception On errors
   */

  public AzDatabaseType createDatabase()
    throws Exception
  {
    return DATABASES.open(
      this.configuration,
      message -> {

      });
  }

  /**
   * Reset the container by dropping and recreating the database. This
   * is significantly faster than destroying and recreating the container.
   *
   * @throws IOException          On errors
   * @throws InterruptedException On interruption
   */

  public void reset()
    throws IOException, InterruptedException
  {
    {
      final var r =
        this.postgres.container()
          .executeAndWait(
            List.of(
              "dropdb",
              "-f",
              "-w",
              "-U",
              this.postgres.databaseOwner(),
              "azeno"
            ),
            10L,
            TimeUnit.SECONDS
          );
      Assertions.assertEquals(0, r);
    }

    {
      final var r =
        this.postgres.container()
          .executeAndWait(
            List.of(
              "createdb",
              "-w",
              "-U",
              this.postgres.databaseOwner(),
              "azeno"
            ),
            10L,
            TimeUnit.SECONDS
          );
      Assertions.assertEquals(0, r);
    }
  }
}
