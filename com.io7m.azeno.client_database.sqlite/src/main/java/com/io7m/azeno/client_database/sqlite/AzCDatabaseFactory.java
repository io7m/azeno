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


package com.io7m.azeno.client_database.sqlite;

import com.io7m.azeno.client_database.api.AzCDatabaseConfiguration;
import com.io7m.azeno.client_database.api.AzCDatabaseConnectionType;
import com.io7m.azeno.client_database.api.AzCDatabaseFactoryType;
import com.io7m.azeno.client_database.api.AzCDatabaseQueryProviderType;
import com.io7m.azeno.client_database.api.AzCDatabaseTransactionType;
import com.io7m.azeno.client_database.api.AzCDatabaseType;
import com.io7m.azeno.client_database.sqlite.internal.AzCDatabase;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.sqlite.DSDatabaseFactory;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.lanark.core.RDottedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.io.InputStream;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * The main database factory.
 */

public final class AzCDatabaseFactory
  extends DSDatabaseFactory<
  AzCDatabaseConfiguration,
  AzCDatabaseConnectionType,
  AzCDatabaseTransactionType,
  AzCDatabaseQueryProviderType<?, ?, ?>,
  AzCDatabaseType>
  implements AzCDatabaseFactoryType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AzCDatabaseFactory.class);

  /**
   * The main database factory.
   */

  public AzCDatabaseFactory()
  {

  }

  @Override
  protected RDottedName applicationId()
  {
    return new RDottedName("com.io7m.azeno.client");
  }

  @Override
  protected Logger logger()
  {
    return LOG;
  }

  @Override
  protected AzCDatabaseType onCreateDatabase(
    final AzCDatabaseConfiguration configuration,
    final SQLiteDataSource source,
    final List<AzCDatabaseQueryProviderType<?, ?, ?>> queryProviders,
    final CloseableCollectionType<DDatabaseException> resources)
  {
    return new AzCDatabase(
      configuration,
      source,
      queryProviders,
      resources
    );
  }

  @Override
  protected InputStream onRequireDatabaseSchemaXML()
  {
    return AzCDatabaseFactory.class.getResourceAsStream(
      "/com/io7m/azeno/client_database/sqlite/internal/database.xml"
    );
  }

  @Override
  protected void onEvent(
    final String message)
  {

  }

  @Override
  protected void onAdjustSQLiteConfig(
    final SQLiteConfig config)
  {

  }

  @Override
  protected List<AzCDatabaseQueryProviderType<?, ?, ?>> onRequireDatabaseQueryProviders()
  {
    return ServiceLoader.load(AzCDatabaseQueryProviderType.class)
      .stream()
      .map(ServiceLoader.Provider::get)
      .map(x -> (AzCDatabaseQueryProviderType<?, ?, ?>) x)
      .collect(Collectors.toList());
  }
}
