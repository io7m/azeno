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

import com.io7m.azeno.database.api.AzDatabaseFactoryType;
import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.postgres.AzDatabaseFactory;

/**
 * Digital asset system (Postgres database)
 */

module com.io7m.azeno.database.postgres
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires com.io7m.azeno.database.api;
  requires com.io7m.azeno.error_codes;
  requires com.io7m.azeno.model;
  requires com.io7m.azeno.strings;
  requires com.io7m.azeno.xml;

  requires com.io7m.anethum.api;
  requires com.io7m.darco.api;
  requires com.io7m.darco.postgres;
  requires com.io7m.huanuco.api;
  requires com.io7m.idstore.model;
  requires com.io7m.jmulticlose.core;
  requires com.io7m.jqpage.core;
  requires com.io7m.lanark.core;
  requires com.io7m.medrina.api;
  requires com.io7m.trasco.api;
  requires com.io7m.trasco.vanilla;
  requires io.opentelemetry.api;
  requires io.opentelemetry.context;
  requires io.opentelemetry.semconv;
  requires java.sql;
  requires org.jooq.postgres.extensions;
  requires org.jooq;
  requires org.postgresql.jdbc;
  requires org.slf4j;

  provides AzDatabaseFactoryType with
    AzDatabaseFactory;

  provides AzDatabaseQueryProviderType with
    com.io7m.azeno.database.postgres.internal.AzSchemaGet,
    com.io7m.azeno.database.postgres.internal.AzSchemaPut,
    com.io7m.azeno.database.postgres.internal.AzSchemaSearch,
    com.io7m.azeno.database.postgres.internal.AzAssetGet,
    com.io7m.azeno.database.postgres.internal.AzAssetPut,
    com.io7m.azeno.database.postgres.internal.AzAuditEventPut,
    com.io7m.azeno.database.postgres.internal.AzCollectionAccessGet,
    com.io7m.azeno.database.postgres.internal.AzCollectionAccessSet,
    com.io7m.azeno.database.postgres.internal.AzCollectionGet,
    com.io7m.azeno.database.postgres.internal.AzCollectionPut,
    com.io7m.azeno.database.postgres.internal.AzCollectionSearch,
    com.io7m.azeno.database.postgres.internal.AzStoreGet,
    com.io7m.azeno.database.postgres.internal.AzStorePut,
    com.io7m.azeno.database.postgres.internal.AzStoreSearch,
    com.io7m.azeno.database.postgres.internal.AzUserGet,
    com.io7m.azeno.database.postgres.internal.AzUserPut,
    com.io7m.azeno.database.postgres.internal.AzUserSearch;

  uses AzDatabaseQueryProviderType;

  exports com.io7m.azeno.database.postgres.internal.tables
    to org.jooq;
  exports com.io7m.azeno.database.postgres.internal
    to org.jooq, com.io7m.azeno.tests;

  exports com.io7m.azeno.database.postgres;
}
