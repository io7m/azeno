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


package com.io7m.azeno.database.postgres;

import com.io7m.azeno.database.api.AzDatabaseConfiguration;
import com.io7m.azeno.database.api.AzDatabaseConnectionType;
import com.io7m.azeno.database.api.AzDatabaseFactoryType;
import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.database.api.AzDatabaseType;
import com.io7m.azeno.database.postgres.internal.AzDatabase;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.postgres.DPQDatabaseFactory;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.lanark.core.RDottedName;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static org.jooq.SQLDialect.POSTGRES;

/**
 * The main database factory.
 */

public final class AzDatabaseFactory
  extends DPQDatabaseFactory<
  AzDatabaseConfiguration,
  AzDatabaseConnectionType,
  AzDatabaseTransactionType,
  AzDatabaseQueryProviderType<?, ?, ?>,
  AzDatabaseType>
  implements AzDatabaseFactoryType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AzDatabaseFactory.class);

  /**
   * The main database factory.
   */

  public AzDatabaseFactory()
  {

  }

  @Override
  protected RDottedName applicationId()
  {
    return new RDottedName("com.io7m.azeno.postgresql");
  }

  @Override
  protected Logger logger()
  {
    return LOG;
  }

  @Override
  protected AzDatabaseType onCreateDatabase(
    final AzDatabaseConfiguration configuration,
    final DataSource source,
    final List<AzDatabaseQueryProviderType<?, ?, ?>> queryProviders,
    final CloseableCollectionType<DDatabaseException> resources)
  {
    return new AzDatabase(
      configuration,
      source,
      queryProviders,
      resources
    );
  }

  @Override
  protected InputStream onRequireDatabaseSchemaXML()
  {
    return AzDatabaseFactory.class.getResourceAsStream(
      "/com/io7m/azeno/database/postgres/internal/database.xml"
    );
  }

  @Override
  protected void onEvent(
    final String message)
  {

  }

  @Override
  protected DataSource onTransformDataSourceForSetup(
    final DataSource dataSource)
  {
    return dataSource;
  }

  @Override
  protected DataSource onTransformDataSourceForUse(
    final DataSource dataSource)
  {
    return dataSource;
  }

  @Override
  protected void onPostUpgrade(
    final AzDatabaseConfiguration configuration,
    final Connection connection)
    throws SQLException
  {
    updateReadOnlyRolePassword(configuration, connection);
    updateWorkerRolePassword(configuration, connection);
  }

  @Override
  protected List<AzDatabaseQueryProviderType<?, ?, ?>> onRequireDatabaseQueryProviders()
  {
    return ServiceLoader.load(AzDatabaseQueryProviderType.class)
      .stream()
      .map(ServiceLoader.Provider::get)
      .map(x -> (AzDatabaseQueryProviderType<?, ?, ?>) x)
      .collect(Collectors.toList());
  }

  /**
   * Update the read-only role password. If no password is specified, then
   * logging in is prevented.
   */

  private static void updateReadOnlyRolePassword(
    final AzDatabaseConfiguration configuration,
    final Connection connection)
    throws SQLException
  {
    final var info =
      configuration.info();
    final var passwordOpt =
      info.readerRolePassword();

    if (passwordOpt.isPresent()) {
      LOG.debug("Updating azeno_read_only role to allow password logins");

      final var passwordText =
        passwordOpt.get();
      final var settings =
        new Settings().withRenderNameCase(RenderNameCase.LOWER);
      final var dslContext =
        DSL.using(connection, POSTGRES, settings);

      dslContext.execute(
        "ALTER ROLE azeno_read_only WITH PASSWORD {0}",
        DSL.inline(passwordText)
      );

      try (var st = connection.createStatement()) {
        st.execute("ALTER ROLE azeno_read_only LOGIN");
      }
    } else {
      LOG.debug("updating azeno_read_only role to disallow logins");
      try (var st = connection.createStatement()) {
        st.execute("ALTER ROLE azeno_read_only NOLOGIN");
      }
    }
  }

  /**
   * Update the worker role password. Might be a no-op.
   */

  private static void updateWorkerRolePassword(
    final AzDatabaseConfiguration configuration,
    final Connection connection)
    throws SQLException
  {
    LOG.debug("Updating azeno role");

    final var info =
      configuration.info();
    final var passwordText =
      info.workerRolePassword();
    final var settings =
      new Settings().withRenderNameCase(RenderNameCase.LOWER);
    final var dslContext =
      DSL.using(connection, POSTGRES, settings);
    dslContext.execute(
      "ALTER ROLE azeno WITH PASSWORD {0}",
      DSL.inline(passwordText)
    );

    try (var st = connection.createStatement()) {
      st.execute("ALTER ROLE azeno LOGIN");
    }
  }
}
