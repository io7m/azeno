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

import com.io7m.azeno.database.api.AzDatabaseConfiguration;
import com.io7m.azeno.database.api.AzDatabaseConnectionType;
import com.io7m.azeno.database.api.AzDatabaseQueryProviderType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.model.AzUserID;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.api.DDatabaseTransactionAbstract;
import com.io7m.darco.api.DDatabaseTransactionCloseBehavior;
import io.opentelemetry.api.trace.Span;

import java.util.Map;
import java.util.Optional;

final class AzDatabaseTransaction
  extends DDatabaseTransactionAbstract<
  AzDatabaseConfiguration,
  AzDatabaseConnectionType,
  AzDatabaseTransactionType,
  AzDatabaseQueryProviderType<?, ?, ?>>
  implements AzDatabaseTransactionType
{
  private Optional<AzUserID> userId;

  AzDatabaseTransaction(
    final DDatabaseTransactionCloseBehavior closeBehavior,
    final AzDatabaseConfiguration inConfiguration,
    final AzDatabaseConnectionType inConnection,
    final Span inTransactionScope,
    final Map<Class<?>, AzDatabaseQueryProviderType<?, ?, ?>> inQueries)
  {
    super(
      closeBehavior,
      inConfiguration,
      inConnection,
      inTransactionScope,
      inQueries
    );
  }

  @Override
  public void setUserID(
    final AzUserID id)
  {
    this.userId = Optional.of(id);
  }

  @Override
  public AzUserID userId()
    throws DDatabaseException
  {
    return this.userId.orElseThrow(() -> {
      return new DDatabaseException(
        "No user ID has been set.",
        "error-api-misuse",
        Map.of(),
        Optional.empty()
      );
    });
  }
}
