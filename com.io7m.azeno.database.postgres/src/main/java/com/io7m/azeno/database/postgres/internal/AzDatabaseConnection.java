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

final class AzDatabaseConnection
  extends DDatabaseConnectionAbstract<
  AzDatabaseConfiguration,
  AzDatabaseTransactionType,
  AzDatabaseQueryProviderType<?, ?, ?>>
  implements AzDatabaseConnectionType
{
  private static final Settings SETTINGS =
    new Settings().withRenderNameCase(RenderNameCase.LOWER);

  private final AzStrings strings;

  AzDatabaseConnection(
    final AzDatabaseType database,
    final AzStrings inStrings,
    final Span span,
    final Connection connection,
    final Map<Class<?>, AzDatabaseQueryProviderType<?, ?, ?>> queries)
  {
    super(database.configuration(), span, connection, queries);
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
  }

  @Override
  protected AzDatabaseTransactionType createTransaction(
    final DDatabaseTransactionCloseBehavior closeBehavior,
    final Span transactionSpan,
    final Map<Class<?>, AzDatabaseQueryProviderType<?, ?, ?>> queries)
  {
    final var transaction = new AzDatabaseTransaction(
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
    return DSL.using(this.connection(), SQLDialect.POSTGRES, SETTINGS);
  }
}
