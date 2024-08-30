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


package com.io7m.azeno.client_database.sqlite.internal;

import com.io7m.azeno.client_database.api.AzCDatabaseConfiguration;
import com.io7m.azeno.client_database.api.AzCDatabaseConnectionType;
import com.io7m.azeno.client_database.api.AzCDatabaseQueryProviderType;
import com.io7m.azeno.client_database.api.AzCDatabaseTransactionType;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DDatabaseConnectionAbstract;
import com.io7m.darco.api.DDatabaseTransactionCloseBehavior;
import io.opentelemetry.api.trace.Span;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.Map;
import java.util.Objects;

final class AzCDatabaseConnection
  extends DDatabaseConnectionAbstract<
  AzCDatabaseConfiguration,
  AzCDatabaseTransactionType,
  AzCDatabaseQueryProviderType<?, ?, ?>>
  implements AzCDatabaseConnectionType
{
  private static final Settings SETTINGS =
    new Settings().withRenderNameCase(RenderNameCase.LOWER);

  private final AzStrings strings;

  AzCDatabaseConnection(
    final AzCDatabase database,
    final AzStrings inStrings,
    final Span span,
    final Connection connection,
    final Map<Class<?>, AzCDatabaseQueryProviderType<?, ?, ?>> queries)
  {
    super(database.configuration(), span, connection, queries);
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
  }

  @Override
  protected AzCDatabaseTransactionType createTransaction(
    final DDatabaseTransactionCloseBehavior closeBehavior,
    final Span transactionSpan,
    final Map<Class<?>, AzCDatabaseQueryProviderType<?, ?, ?>> queries)
  {
    final var transaction = new AzCDatabaseTransaction(
      closeBehavior,
      this.configuration(),
      this,
      transactionSpan,
      queries
    );

    transaction.put(DSLContext.class, this.createContext());
    transaction.put(AzStrings.class, this.strings);
    return transaction;
  }

  private DSLContext createContext()
  {
    return DSL.using(this.connection(), SQLDialect.SQLITE, SETTINGS);
  }
}
