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

import com.io7m.anethum.api.SerializationException;
import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.database.api.AzSchemaPutType;
import com.io7m.azeno.model.AzAuditEvent;
import com.io7m.azeno.model.AzSchema;
import com.io7m.azeno.model.AzUnit;
import com.io7m.darco.api.DDatabaseException;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;

import static com.io7m.azeno.database.postgres.internal.AZDatabaseExceptions.handleDatabaseException;
import static com.io7m.azeno.database.postgres.internal.Tables.SCHEMAS;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorIo;

/**
 * SchemaPut.
 */

public final class AzSchemaPut
  extends AzDatabaseQueryAbstract<AzSchema, AzUnit>
  implements AzSchemaPutType
{
  AzSchemaPut(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  /**
   * @return The query provider
   */

  public static AzDatabaseQueryProviderType<AzSchema, AzUnit, AzSchemaPutType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzSchemaPutType.class,
      AzSchemaPut::new
    );
  }

  @Override
  protected AzUnit onExecute(
    final AzDatabaseTransactionType transaction,
    final AzSchema schema)
    throws DDatabaseException
  {
    this.putAttribute("Schema Name", schema.id().name());
    this.putAttribute("Schema Version", schema.id().version());

    final var context =
      transaction.get(DSLContext.class);

    try {
      final var dataText =
        serializeSchema(schema);

      context.insertInto(SCHEMAS)
        .set(SCHEMAS.SCHEMA_NAME, schema.id().name().value())
        .set(SCHEMAS.SCHEMA_VERSION, schema.id().version())
        .set(SCHEMAS.SCHEMA_DATA_TYPE, "com.io7m.azeno.xml:1")
        .set(SCHEMAS.SCHEMA_DATA, dataText)
        .execute();

      putAuditEvent(
        context,
        new AzAuditEvent(
          0L,
          OffsetDateTime.now(),
          transaction.userId(),
          "SCHEMA_CREATED",
          this.attributes()
        ));

      return AzUnit.UNIT;
    } catch (final DataAccessException e) {
      throw handleDatabaseException(transaction, this.attributes(), e);
    } catch (final SerializationException | IOException e) {
      throw new DDatabaseException(
        e.getMessage(),
        e,
        errorIo().id(),
        this.attributes(),
        Optional.empty()
      );
    }
  }

  private static String serializeSchema(
    final AzSchema schema)
    throws SerializationException, IOException
  {
    try (final var outputStream = new ByteArrayOutputStream()) {
      AzXML.schemaSerializers()
        .serialize(
          URI.create("urn:output"),
          outputStream,
          schema
        );
      outputStream.flush();
      return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }
  }
}
