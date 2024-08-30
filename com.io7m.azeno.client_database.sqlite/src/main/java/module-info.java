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

import com.io7m.azeno.client_database.api.AzCDatabaseQueryProviderType;

/**
 * Digital asset system (Client Database)
 */

module com.io7m.azeno.client_database.sqlite
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires com.io7m.azeno.client_database.api;
  requires com.io7m.azeno.strings;

  requires com.io7m.darco.api;
  requires com.io7m.darco.sqlite;
  requires com.io7m.jmulticlose.core;
  requires com.io7m.lanark.core;
  requires io.opentelemetry.api;
  requires org.jooq;
  requires org.slf4j;
  requires org.xerial.sqlitejdbc;

  uses com.io7m.azeno.client_database.api.AzCDatabaseQueryProviderType;

  provides AzCDatabaseQueryProviderType with
    com.io7m.azeno.client_database.sqlite.internal.AzCBookmarkDelete,
    com.io7m.azeno.client_database.sqlite.internal.AzCBookmarkGet,
    com.io7m.azeno.client_database.sqlite.internal.AzCBookmarkList,
    com.io7m.azeno.client_database.sqlite.internal.AzCBookmarkPut
    ;

  exports com.io7m.azeno.client_database.sqlite.internal.tables
    to org.jooq;
  exports com.io7m.azeno.client_database.sqlite.internal
    to org.jooq, com.io7m.azeno.tests;

  exports com.io7m.azeno.client_database.sqlite;
}
