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

import com.io7m.azeno.tests.AzTestProperties;
import com.io7m.ervilla.api.EContainerFactoryType;
import com.io7m.ervilla.api.EContainerType;
import com.io7m.ervilla.api.EPortAddressType;
import com.io7m.ervilla.postgres.EPgSpecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A PostgreSQL fixture.
 */

public final class AzPostgresFixture
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AzIdstoreFixture.class);

  private final EContainerType container;
  private final String databaseOwner;
  private final int port;

  private AzPostgresFixture(
    final EContainerType inContainer,
    final String inDatabaseOwner,
    final int inPort)
  {
    this.container =
      Objects.requireNonNull(inContainer, "container");
    this.databaseOwner =
      Objects.requireNonNull(inDatabaseOwner, "databaseOwner");
    this.port =
      inPort;
  }

  public static AzPostgresFixture create(
    final EContainerFactoryType supervisor,
    final int port)
    throws Exception
  {
    LOG.info(
      "Creating postgres database on {}", Integer.valueOf(port));

    final var builder =
      EPgSpecs.builderFromDockerIO(
        AzTestProperties.POSTGRESQL_VERSION,
        new EPortAddressType.All(),
        port,
        "postgres",
        "postgres",
        "12345678"
      );

    if (Objects.equals(System.getenv("AZENO_POSTGRES_TEST_DEBUG"), "true")) {
      builder.addArgument("-c")
        .addArgument("log_destination=stderr")
        .addArgument("-c")
        .addArgument("log_statement=all")
        .addArgument("-c")
        .addArgument("client_min_messages=log");
    }

    return new AzPostgresFixture(
      supervisor.start(builder.build()),
      "postgres",
      port
    );
  }

  public EContainerType container()
  {
    return this.container;
  }

  public String databaseOwner()
  {
    return this.databaseOwner;
  }

  public int port()
  {
    return this.port;
  }
}
