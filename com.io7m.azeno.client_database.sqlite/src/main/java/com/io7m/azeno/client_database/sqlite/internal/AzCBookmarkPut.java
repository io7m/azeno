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

import com.io7m.azeno.client_database.api.AzCBookmark;
import com.io7m.azeno.client_database.api.AzCBookmarkPutType;
import com.io7m.azeno.client_database.api.AzCDatabaseQueryProviderType;
import com.io7m.azeno.client_database.api.AzCDatabaseTransactionType;
import com.io7m.darco.api.DDatabaseUnit;
import org.jooq.DSLContext;

import static com.io7m.azeno.client_database.sqlite.internal.Tables.BOOKMARKS;

/**
 * BookmarkPut
 */

public final class AzCBookmarkPut
  extends AzCDatabaseQueryAbstract<AzCBookmark, DDatabaseUnit>
  implements AzCBookmarkPutType
{
  AzCBookmarkPut(
    final AzCDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static AzCDatabaseQueryProviderType<AzCBookmark, DDatabaseUnit, AzCBookmarkPutType>
  provider()
  {
    return AzCDatabaseQueryProvider.provide(
      AzCBookmarkPutType.class,
      AzCBookmarkPut::new
    );
  }

  @Override
  protected DDatabaseUnit onExecute(
    final AzCDatabaseTransactionType transaction,
    final AzCBookmark bookmark)
  {
    final var context =
      transaction.get(DSLContext.class);

    context.insertInto(BOOKMARKS)
      .set(BOOKMARKS.BOOKMARK_HOST, bookmark.host())
      .set(BOOKMARKS.BOOKMARK_NAME, bookmark.name())
      .set(BOOKMARKS.BOOKMARK_PASSWORD, bookmark.password())
      .set(BOOKMARKS.BOOKMARK_PORT, bookmark.port())
      .set(BOOKMARKS.BOOKMARK_TLS, bookmark.useTLS() ? 1 : 0)
      .set(BOOKMARKS.BOOKMARK_USER, bookmark.user())
      .onDuplicateKeyUpdate()
      .set(BOOKMARKS.BOOKMARK_HOST, bookmark.host())
      .set(BOOKMARKS.BOOKMARK_NAME, bookmark.name())
      .set(BOOKMARKS.BOOKMARK_PASSWORD, bookmark.password())
      .set(BOOKMARKS.BOOKMARK_PORT, bookmark.port())
      .set(BOOKMARKS.BOOKMARK_TLS, bookmark.useTLS() ? 1 : 0)
      .set(BOOKMARKS.BOOKMARK_USER, bookmark.user())
      .execute();

    return DDatabaseUnit.UNIT;
  }
}
