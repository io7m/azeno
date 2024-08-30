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

import com.io7m.azeno.database.api.AzDatabaseStoreSearchType;
import com.io7m.azeno.model.AzPage;
import com.io7m.azeno.model.AzStoreID;
import com.io7m.azeno.model.AzStoreSummary;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.jqpage.core.JQKeysetRandomAccessPageDefinition;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import java.util.List;
import java.util.Map;

import static com.io7m.azeno.database.postgres.internal.Tables.STORES;
import static io.opentelemetry.semconv.trace.attributes.SemanticAttributes.DB_STATEMENT;

final class AzStoreSearchPaged
  extends AzAbstractSearch<AzStoreSummary>
  implements AzDatabaseStoreSearchType
{
  AzStoreSearchPaged(
    final List<JQKeysetRandomAccessPageDefinition> inPages)
  {
    super(inPages);
  }

  @Override
  protected AzPage<AzStoreSummary> page(
    final AzDatabaseTransaction transaction,
    final JQKeysetRandomAccessPageDefinition page)
    throws DDatabaseException
  {
    final var context =
      transaction.get(DSLContext.class);

    final var querySpan =
      transaction.createSubSpan("AzStoreSearch.page");

    try {
      final var query =
        page.queryFields(context, List.of(
          STORES.STORE_ID,
          STORES.STORE_TITLE
        ));

      querySpan.setAttribute(DB_STATEMENT, query.toString());

      final var items =
        query.fetch().map(record -> {
          return new AzStoreSummary(
            new AzStoreID(record.get(STORES.STORE_ID)),
            record.get(STORES.STORE_TITLE)
          );
        });

      return new AzPage<>(
        items,
        (int) page.index(),
        this.pageCount(),
        page.firstOffset()
      );
    } catch (final DataAccessException e) {
      querySpan.recordException(e);
      throw AZDatabaseExceptions.handleDatabaseException(
        transaction,
        Map.of(),
        e
      );
    } finally {
      querySpan.end();
    }
  }
}
