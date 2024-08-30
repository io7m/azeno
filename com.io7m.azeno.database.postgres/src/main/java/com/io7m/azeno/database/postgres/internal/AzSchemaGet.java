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

import com.io7m.anethum.api.ParsingException;
import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.database.api.AzSchemaGetType;
import com.io7m.azeno.model.AzSchema;
import com.io7m.azeno.model.AzSchemaID;
import com.io7m.darco.api.DDatabaseException;
import org.jooq.DSLContext;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Optional;

import static com.io7m.azeno.database.postgres.internal.Tables.SCHEMAS;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorIo;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * SchemaGet.
 */

public final class AzSchemaGet
  extends AzDatabaseQueryAbstract<AzSchemaID, Optional<AzSchema>>
  implements AzSchemaGetType
{
  AzSchemaGet(
    final AzDatabaseTransactionType transaction)
  {
    super(transaction);
  }

  /**
   * @return The query provider
   */

  public static AzDatabaseQueryProviderType<AzSchemaID, Optional<AzSchema>, AzSchemaGetType>
  provider()
  {
    return AzDatabaseQueryProvider.provide(
      AzSchemaGetType.class,
      AzSchemaGet::new
    );
  }

  @Override
  protected Optional<AzSchema> onExecute(
    final AzDatabaseTransactionType transaction,
    final AzSchemaID id)
    throws DDatabaseException
  {
    final var context =
      transaction.get(DSLContext.class);

    final var schemaMatches =
      SCHEMAS.SCHEMA_NAME.eq(id.name().value())
        .and(SCHEMAS.SCHEMA_VERSION.eq(id.version()));

    final var recOpt =
      context.select(
          SCHEMAS.SCHEMA_NAME,
          SCHEMAS.SCHEMA_VERSION,
          SCHEMAS.SCHEMA_DATA_TYPE,
          SCHEMAS.SCHEMA_DATA)
        .from(SCHEMAS)
        .where(schemaMatches)
        .fetchOptional();

    if (recOpt.isEmpty()) {
      return Optional.empty();
    }

    try {
      return Optional.of(mapRecord(recOpt.get()));
    } catch (final ParsingException e) {
      throw new DDatabaseException(
        e.getMessage(),
        e,
        errorIo().id(),
        this.attributes(),
        Optional.empty()
      );
    }
  }

  private static AzSchema mapRecord(
    final org.jooq.Record x)
    throws ParsingException
  {
    return schemaOf(
      x.get(SCHEMAS.SCHEMA_DATA_TYPE),
      x.get(SCHEMAS.SCHEMA_DATA)
    );
  }

  private static AzSchema schemaOf(
    final String schemaDataType,
    final String schemaData)
    throws ParsingException
  {
    final var parser =
      AzXML.schemaParsers();

    return parser.parse(
      URI.create("urn:db"),
      new ByteArrayInputStream(schemaData.getBytes(UTF_8))
    );
  }
}
