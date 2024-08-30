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

import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseSchemaSearchType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.database.api.AzSchemaSearchType;
import com.io7m.azeno.model.AzUnit;
import com.io7m.jqpage.core.JQField;
import com.io7m.jqpage.core.JQKeysetRandomAccessPagination;
import com.io7m.jqpage.core.JQKeysetRandomAccessPaginationParameters;
import com.io7m.jqpage.core.JQOrder;
import io.opentelemetry.api.trace.Span;
import org.jooq.DSLContext;

import static com.io7m.azeno.database.postgres.internal.Tables.SCHEMAS;
import static io.opentelemetry.semconv.trace.attributes.SemanticAttributes.DB_STATEMENT;

/**
 * SchemaSearch.
 */

public final class AzSchemaSearch
  extends AzDatabaseQueryAbstract<AzUnit, AzDatabaseSchemaSearchType>
  implements AzSchemaSearchType
{
  AzSchemaSearch(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  @Override
  protected AzDatabaseSchemaSearchType onExecute(
    final AzDatabaseTransactionType transaction,
    final AzUnit parameters)
  {
    final var context =
      transaction.get(DSLContext.class);

    final var pageParameters =
      JQKeysetRandomAccessPaginationParameters.forTable(SCHEMAS)
        .addSortField(new JQField(SCHEMAS.SCHEMA_NAME, JQOrder.ASCENDING))
        .setPageSize(1000L)
        .setStatementListener(statement -> {
          Span.current().setAttribute(DB_STATEMENT, statement.toString());
        }).build();

    final var pages =
      JQKeysetRandomAccessPagination.createPageDefinitions(
        context, pageParameters);

    return new AzSchemaSearchPaged(pages);
  }

  /**
   * @return The query provider
   */

  public static AzDatabaseQueryProviderType<AzUnit, AzDatabaseSchemaSearchType, AzSchemaSearchType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzSchemaSearchType.class,
      AzSchemaSearch::new
    );
  }
}
