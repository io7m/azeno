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
import com.io7m.azeno.client_database.api.AzCBookmarkGetType;
import com.io7m.azeno.client_database.api.AzCDatabaseQueryProviderType;
import com.io7m.azeno.client_database.api.AzCDatabaseTransactionType;
import org.jooq.DSLContext;

import java.util.Optional;

import static com.io7m.azeno.client_database.sqlite.internal.Tables.BOOKMARKS;

/**
 * BookmarkGet
 */

public final class AzCBookmarkGet
  extends AzCDatabaseQueryAbstract<String, Optional<AzCBookmark>>
  implements AzCBookmarkGetType
{
  AzCBookmarkGet(
    final AzCDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static AzCDatabaseQueryProviderType<String, Optional<AzCBookmark>, AzCBookmarkGetType>
  provider()
  {
    return AzCDatabaseQueryProvider.provide(
      AzCBookmarkGetType.class,
      AzCBookmarkGet::new
    );
  }

  @Override
  protected Optional<AzCBookmark> onExecute(
    final AzCDatabaseTransactionType transaction,
    final String name)
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
      .where(BOOKMARKS.BOOKMARK_NAME.eq(name))
      .fetchOptional()
      .map(AzCBookmarkGet::mapRecord);
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
