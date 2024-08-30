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

import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.error_codes.AzErrorCode;
import com.io7m.azeno.error_codes.AzStandardErrorCodes;
import com.io7m.azeno.strings.AzStringConstants;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DDatabaseException;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.UniqueKey;
import org.jooq.exception.DataAccessException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorNonexistent;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorOperationNotPermitted;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorProtocol;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorSql;
import static java.util.Locale.ROOT;

final class AZDatabaseExceptions
{
  private static final Map<String, AzForeignKeyViolation> FOREIGN_KEYS =
    Stream.of(
      new AzForeignKeyViolation(
        Keys.COLLECTIONS_ACCESS_COLLECTION_EXISTS,
        AzStandardErrorCodes.errorNonexistent(),
        AzStringConstants.ERROR_NONEXISTENT_COLLECTION
      ),
      new AzForeignKeyViolation(
        Keys.COLLECTION_STORE_EXISTS,
        AzStandardErrorCodes.errorNonexistent(),
        AzStringConstants.ERROR_NONEXISTENT_STORE
      ),
      new AzForeignKeyViolation(
        Keys.COLLECTIONS_ACCESS_USER_EXISTS,
        AzStandardErrorCodes.errorNonexistent(),
        AzStringConstants.ERROR_NONEXISTENT_USER
      )
    ).collect(Collectors.toMap(e -> {
      return e.foreignKey.getName().toUpperCase(ROOT);
    }, e -> e));

  private static final Map<String, AzUniqueKeyViolation> UNIQUE_KEYS =
    Stream.<AzUniqueKeyViolation>of(
    ).collect(Collectors.toMap(e -> {
      return e.uniqueKey.getName().toUpperCase(ROOT);
    }, e -> e));

  private record AzForeignKeyViolation(
    ForeignKey<Record, Record> foreignKey,
    AzErrorCode errorCode,
    AzStringConstants errorMessage)
  {
    AzForeignKeyViolation
    {
      Objects.requireNonNull(foreignKey, "foreignKey");
      Objects.requireNonNull(errorCode, "errorCode");
      Objects.requireNonNull(errorMessage, "errorMessage");
    }
  }

  private record AzUniqueKeyViolation(
    UniqueKey<Record> uniqueKey,
    AzErrorCode errorCode,
    AzStringConstants errorMessage)
  {
    AzUniqueKeyViolation
    {
      Objects.requireNonNull(uniqueKey, "uniqueKey");
      Objects.requireNonNull(errorCode, "errorCode");
      Objects.requireNonNull(errorMessage, "errorMessage");
    }
  }

  private AZDatabaseExceptions()
  {

  }

  static DDatabaseException handleDatabaseException(
    final AzDatabaseTransactionType transaction,
    final Map<String, String> attributes,
    final DataAccessException e)
  {
    /*
     * See https://www.postgresql.org/docs/current/errcodes-appendix.html
     * for all of these numeric codes.
     */

    final DDatabaseException result = switch (e.sqlState()) {
      /*
       * foreign_key_violation
       */

      case "23502" -> integrityViolation(transaction, e, attributes);

      /*
       * foreign_key_violation
       */

      case "23503" -> handleForeignKeyViolation(transaction, e, attributes);

      /*
       * unique_violation
       */

      case "23505" -> handleUniqueViolation(transaction, e, attributes);

      /*
       * PostgreSQL: character_not_in_repertoire
       */

      case "22021" -> handleCharacterEncoding(e, attributes);

      /*
       * insufficient_privilege
       */

      case "42501" -> {
        yield new DDatabaseException(
          e.getMessage(),
          e,
          errorOperationNotPermitted().id(),
          attributes,
          Optional.empty()
        );
      }

      default -> {
        yield fallbackException(attributes, e);
      }
    };

    try {
      transaction.rollback();
    } catch (final DDatabaseException ex) {
      result.addSuppressed(ex);
    }
    return result;
  }

  private static DDatabaseException fallbackException(
    final Map<String, String> attributes,
    final Exception e)
  {
    return new DDatabaseException(
      e.getMessage(),
      e,
      errorSql().id(),
      attributes,
      Optional.empty()
    );
  }

  private static DDatabaseException integrityViolation(
    final AzDatabaseTransactionType transaction,
    final DataAccessException e,
    final Map<String, String> attributes)
  {
    final var strings =
      transaction.get(AzStrings.class);

    final String constraint =
      findPSQLException(e)
        .flatMap(z -> Optional.ofNullable(z.getServerErrorMessage()))
        .map(ServerErrorMessage::getConstraint)
        .map(x -> x.toUpperCase(ROOT))
        .orElse("");

    final String column =
      findPSQLException(e)
        .flatMap(z -> Optional.ofNullable(z.getServerErrorMessage()))
        .map(ServerErrorMessage::getColumn)
        .map(x -> x.toUpperCase(ROOT))
        .orElse("");

    return switch (constraint) {
      case "COLLECTION_SCHEMA_EXISTS",
           "COLLECTIONS_ACCESS_USER_EXISTS",
           "COLLECTIONS_ACCESS_COLLECTION_EXISTS",
           "COLLECTION_STORE_EXISTS" -> {
        yield new DDatabaseException(
          strings.format(AzStringConstants.ERROR_NONEXISTENT),
          e,
          errorNonexistent().id(),
          attributes,
          Optional.empty()
        );
      }

      default -> {
        switch (column) {
          case "COLLECTION_SCHEMA" -> {
            yield new DDatabaseException(
              strings.format(AzStringConstants.ERROR_NONEXISTENT),
              e,
              errorNonexistent().id(),
              attributes,
              Optional.empty()
            );
          }
          default -> {
            yield fallbackException(attributes, e);
          }
        }
      }
    };
  }

  private static DDatabaseException handleCharacterEncoding(
    final DataAccessException e,
    final Map<String, String> attributes)
  {
    final String message =
      findPSQLException(e)
        .flatMap(z -> Optional.ofNullable(z.getServerErrorMessage()))
        .map(ServerErrorMessage::getMessage)
        .orElseGet(e::getMessage);

    return new DDatabaseException(
      message,
      e,
      errorProtocol().id(),
      attributes,
      Optional.empty()
    );
  }

  private static DDatabaseException handleUniqueViolation(
    final AzDatabaseTransactionType transaction,
    final DataAccessException e,
    final Map<String, String> attributes)
  {
    final var strings =
      transaction.get(AzStrings.class);

    final String constraint =
      findPSQLException(e)
        .flatMap(z -> Optional.ofNullable(z.getServerErrorMessage()))
        .map(ServerErrorMessage::getConstraint)
        .map(x -> x.toUpperCase(ROOT))
        .orElse("");

    switch (constraint) {
      case "SCHEMAS_UNIQUE" -> {
        return new DDatabaseException(
          strings.format(AzStringConstants.ERROR_SCHEMA_ALREADY_EXISTS),
          AzStandardErrorCodes.errorDuplicate().id(),
          attributes,
          Optional.empty()
        );
      }
    }

    return fallbackException(attributes, e);
  }

  private static Optional<PSQLException> findPSQLException(
    final DataAccessException e)
  {
    var x = e.getCause();
    while (x != null) {
      if (x instanceof final PSQLException xx) {
        return Optional.of(xx);
      }
      x = x.getCause();
    }
    return Optional.empty();
  }

  private static DDatabaseException handleForeignKeyViolation(
    final AzDatabaseTransactionType transaction,
    final DataAccessException e,
    final Map<String, String> attributes)
  {
    final var strings =
      transaction.get(AzStrings.class);

    var cause = e.getCause();
    while (cause != null) {
      if (cause instanceof final PSQLException actual) {
        final var constraint =
          Optional.ofNullable(actual.getServerErrorMessage())
            .flatMap(x -> Optional.ofNullable(x.getConstraint()))
            .map(x -> x.toUpperCase(ROOT))
            .orElse("");

        final var key = FOREIGN_KEYS.get(constraint);
        if (key != null) {
          return new DDatabaseException(
            strings.format(key.errorMessage),
            e,
            key.errorCode.id(),
            attributes,
            Optional.empty()
          );
        }
      }
      cause = cause.getCause();
    }

    return fallbackException(attributes, e);
  }
}
