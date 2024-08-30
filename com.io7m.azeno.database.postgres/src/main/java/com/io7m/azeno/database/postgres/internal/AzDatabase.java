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


package com.io7m.azeno.database.postgres.internal;

import com.io7m.azeno.database.api.AzDatabaseConfiguration;
import com.io7m.azeno.database.api.AzDatabaseConnectionType;
import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.database.api.AzDatabaseType;
import com.io7m.darco.api.DDatabaseAbstract;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import io.opentelemetry.api.trace.Span;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

/**
 * The main database.
 */

public final class AzDatabase
  extends DDatabaseAbstract<
  AzDatabaseConfiguration,
  AzDatabaseConnectionType,
  AzDatabaseTransactionType,
  AzDatabaseQueryProviderType<?, ?, ?>>
  implements AzDatabaseType
{
  /**
   * The main database.
   *
   * @param inConfiguration  The configuration
   * @param inDataSource     The data source
   * @param inQueryProviders The query providers
   * @param inResources      The resources
   */

  public AzDatabase(
    final AzDatabaseConfiguration inConfiguration,
    final DataSource inDataSource,
    final Collection<AzDatabaseQueryProviderType<?, ?, ?>> inQueryProviders,
    final CloseableCollectionType<DDatabaseException> inResources)
  {
    super(inConfiguration, inDataSource, inQueryProviders, inResources);
  }

  @Override
  protected AzDatabaseConnectionType createConnection(
    final Span span,
    final Connection connection,
    final Map<Class<?>, AzDatabaseQueryProviderType<?, ?, ?>> queries)
  {
    return new AzDatabaseConnection(
      this,
      this.configuration().strings(),
      span,
      connection,
      queries
    );
  }
}
