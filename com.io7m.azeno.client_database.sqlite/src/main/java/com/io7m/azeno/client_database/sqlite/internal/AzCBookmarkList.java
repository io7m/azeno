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
import com.io7m.azeno.client_database.api.AzCBookmarkListType;
import com.io7m.azeno.client_database.api.AzCDatabaseQueryProviderType;
import com.io7m.azeno.client_database.api.AzCDatabaseTransactionType;
import com.io7m.darco.api.DDatabaseUnit;
import org.jooq.DSLContext;

import java.util.List;

import static com.io7m.azeno.client_database.sqlite.internal.Tables.BOOKMARKS;

/**
 * BookmarkList
 */

public final class AzCBookmarkList
  extends AzCDatabaseQueryAbstract<DDatabaseUnit, List<AzCBookmark>>
  implements AzCBookmarkListType
{
  AzCBookmarkList(
    final AzCDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static AzCDatabaseQueryProviderType<DDatabaseUnit, List<AzCBookmark>, AzCBookmarkListType>
  provider()
  {
    return AzCDatabaseQueryProvider.provide(
      AzCBookmarkListType.class,
      AzCBookmarkList::new
    );
  }

  @Override
  protected List<AzCBookmark> onExecute(
    final AzCDatabaseTransactionType transaction,
    final DDatabaseUnit name)
  {
    final var context =
      transaction.get(DSLContext.class);

    return context.select(
        BOOKMARKS.BOOKMARK_HOST,
        BOOKMARKS.BOOKMARK_NAME,
        BOOKMARKS.BOOKMARK_PASSWORD,
        BOOKMARKS.BOOKMARK_PORT,
        BOOKMARKS.BOOKMARK_TLS,
        BOOKMARKS.BOOKMARK_USER
      ).from(BOOKMARKS)
      .orderBy(BOOKMARKS.BOOKMARK_NAME.asc())
      .stream()
      .map(AzCBookmarkList::mapRecord)
      .toList();
  }

  private static AzCBookmark mapRecord(
    final org.jooq.Record r)
  {
    return new AzCBookmark(
      r.get(BOOKMARKS.BOOKMARK_NAME),
      r.get(BOOKMARKS.BOOKMARK_HOST),
      r.get(BOOKMARKS.BOOKMARK_PORT),
      r.get(BOOKMARKS.BOOKMARK_TLS) == 1,
      r.get(BOOKMARKS.BOOKMARK_USER),
      r.get(BOOKMARKS.BOOKMARK_PASSWORD)
    );
  }
}
