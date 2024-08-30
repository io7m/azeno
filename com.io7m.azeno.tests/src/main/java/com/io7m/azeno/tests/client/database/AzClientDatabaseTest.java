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


package com.io7m.azeno.tests.client.database;

import com.io7m.azeno.client_database.api.AzCBookmark;
import com.io7m.azeno.client_database.api.AzCBookmarkDeleteType;
import com.io7m.azeno.client_database.api.AzCBookmarkGetType;
import com.io7m.azeno.client_database.api.AzCBookmarkListType;
import com.io7m.azeno.client_database.api.AzCBookmarkPutType;
import com.io7m.azeno.client_database.api.AzCDatabaseConfiguration;
import com.io7m.azeno.client_database.api.AzCDatabaseTransactionType;
import com.io7m.azeno.client_database.api.AzCDatabaseType;
import com.io7m.azeno.client_database.sqlite.AzCDatabaseFactory;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DDatabaseCreate;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.api.DDatabaseUnit;
import com.io7m.darco.api.DDatabaseUpgrade;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class AzClientDatabaseTest
{
  private Path directory;
  private AzCDatabaseFactory databases;
  private AzCDatabaseType database;
  private AzCDatabaseTransactionType transaction;
  private AzCBookmarkListType bookmarkList;
  private AzCBookmarkPutType bookmarkPut;
  private AzCBookmarkGetType bookmarkGet;
  private AzCBookmarkDeleteType bookmarkDelete;

  @BeforeEach
  public void setup(
    final @TempDir Path inDirectory)
    throws DDatabaseException
  {
    this.directory = inDirectory;
    this.databases = new AzCDatabaseFactory();
    this.database = this.databases.open(
      new AzCDatabaseConfiguration(
        AzStrings.create(Locale.ROOT),
        DDatabaseCreate.CREATE_DATABASE,
        DDatabaseUpgrade.UPGRADE_DATABASE,
        this.directory.resolve("test.db")
      ),
      event -> {

      }
    );
    this.transaction =
      this.database.openTransaction();
    this.bookmarkList =
      this.transaction.query(AzCBookmarkListType.class);
    this.bookmarkPut =
      this.transaction.query(AzCBookmarkPutType.class);
    this.bookmarkGet =
      this.transaction.query(AzCBookmarkGetType.class);
    this.bookmarkDelete =
      this.transaction.query(AzCBookmarkDeleteType.class);
  }

  @AfterEach
  public void tearDown()
    throws DDatabaseException
  {
    this.database.close();
  }

  @Test
  public void testBookmarks()
    throws Exception
  {
    assertEquals(List.of(), this.bookmarkList.execute(DDatabaseUnit.UNIT));

    final var b0 =
      new AzCBookmark(
        "bookmark-0",
        "localhost",
        26000,
        false,
        "someone",
        "12345678");
    final var b1 =
      new AzCBookmark(
        "bookmark-0",
        "localhost-2",
        26000,
        false,
        "someone",
        "12345678");
    final var b2 =
      new AzCBookmark(
        "bookmark-1",
        "localhost",
        26000,
        false,
        "other",
        "12345678");

    this.bookmarkPut.execute(b0);
    this.bookmarkPut.execute(b1);
    this.bookmarkPut.execute(b2);

    assertEquals(
      b1,
      this.bookmarkGet.execute(b0.name()).orElseThrow()
    );
    assertEquals(
      b2,
      this.bookmarkGet.execute(b2.name()).orElseThrow()
    );
    assertEquals(
      List.of(b1, b2),
      this.bookmarkList.execute(DDatabaseUnit.UNIT)
    );

    this.bookmarkDelete.execute("bookmark-0");

    assertEquals(
      Optional.empty(),
      this.bookmarkGet.execute(b0.name())
    );
    assertEquals(
      List.of(b2),
      this.bookmarkList.execute(DDatabaseUnit.UNIT)
    );
    assertEquals(
      b2,
      this.bookmarkGet.execute(b2.name()).orElseThrow()
    );
  }
}
