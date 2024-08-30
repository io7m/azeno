/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.azeno.database.api.AzDatabasePagedQueryType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.model.AzPage;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.jqpage.core.JQKeysetRandomAccessPageDefinition;

import java.util.List;
import java.util.Objects;

/**
 * A convenient abstract class for performing paginated searches.
 *
 * @param <T> The type of returned values
 */

public abstract class AzAbstractSearch<T>
  implements AzDatabasePagedQueryType<T>
{
  private final List<JQKeysetRandomAccessPageDefinition> pages;
  private int pageIndex;

  AzAbstractSearch(
    final List<JQKeysetRandomAccessPageDefinition> inPages)
  {
    this.pages =
      Objects.requireNonNull(inPages, "pages");
    this.pageIndex = 0;
  }

  protected final int pageCount()
  {
    return this.pages.size();
  }

  protected abstract AzPage<T> page(
    AzDatabaseTransaction transaction,
    JQKeysetRandomAccessPageDefinition page)
    throws DDatabaseException;

  @Override
  public final AzPage<T> pageCurrent(
    final AzDatabaseTransactionType transaction)
    throws DDatabaseException
  {
    return this.page(
      (AzDatabaseTransaction) transaction,
      this.pages.get(this.pageIndex)
    );
  }

  @Override
  public final AzPage<T> pageNext(
    final AzDatabaseTransactionType transaction)
    throws DDatabaseException
  {
    final var nextIndex = this.pageIndex + 1;
    final var maxIndex = Math.max(0, this.pages.size() - 1);
    this.pageIndex = Math.min(nextIndex, maxIndex);
    return this.pageCurrent(transaction);
  }

  @Override
  public final AzPage<T> pagePrevious(
    final AzDatabaseTransactionType transaction)
    throws DDatabaseException
  {
    final var prevIndex = this.pageIndex - 1;
    this.pageIndex = Math.max(0, prevIndex);
    return this.pageCurrent(transaction);
  }
}
