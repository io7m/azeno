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

package com.io7m.azeno.server.asset.v1;

import com.io7m.azeno.database.api.AzDatabaseType;
import com.io7m.azeno.error_codes.AzErrorCode;
import com.io7m.azeno.protocol.asset.cb.AzA1Messages;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctionalCoreTransactionalType;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctionalCoreType;
import com.io7m.azeno.server.http.AzHTTPRequestInformation;
import com.io7m.azeno.server.http.AzHTTPResponseType;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import io.helidon.webserver.http.ServerRequest;

import java.util.Objects;

import static com.io7m.azeno.protocol.asset.AzAResponseBlame.BLAME_SERVER;
import static com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType.setSpanErrorCode;

/**
 * A servlet core that executes the given core with a database transaction.
 */

public final class AzA1HandlerCoreTransactional
  implements AzHTTPHandlerFunctionalCoreType
{
  private final AzHTTPHandlerFunctionalCoreTransactionalType core;
  private final AzDatabaseType database;
  private final AzA1Messages messages;

  private AzA1HandlerCoreTransactional(
    final RPServiceDirectoryType services,
    final AzHTTPHandlerFunctionalCoreTransactionalType inCore)
  {
    Objects.requireNonNull(services, "services");

    this.core =
      Objects.requireNonNull(inCore, "core");
    this.database =
      services.requireService(AzDatabaseType.class);
    this.messages =
      services.requireService(AzA1Messages.class);
  }

  /**
   * @param inServices The services
   * @param inCore     The core
   *
   * @return A servlet core that executes the given core with a database
   * transaction
   */

  public static AzHTTPHandlerFunctionalCoreType withTransaction(
    final RPServiceDirectoryType inServices,
    final AzHTTPHandlerFunctionalCoreTransactionalType inCore)
  {
    return new AzA1HandlerCoreTransactional(inServices, inCore);
  }

  @Override
  public AzHTTPResponseType execute(
    final ServerRequest request,
    final AzHTTPRequestInformation information)
  {
    try (var connection = this.database.openConnection()) {
      try (var transaction = connection.openTransaction()) {
        return this.core.executeTransactional(
          request,
          information,
          transaction);
      }
    } catch (final DDatabaseException e) {
      setSpanErrorCode(new AzErrorCode(e.errorCode()));
      return AzA1Errors.errorResponseOf(
        this.messages,
        information,
        BLAME_SERVER,
        e);
    }
  }
}
