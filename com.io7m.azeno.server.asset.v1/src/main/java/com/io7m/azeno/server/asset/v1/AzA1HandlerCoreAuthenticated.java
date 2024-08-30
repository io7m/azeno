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
import com.io7m.azeno.database.api.AzUserGetType;
import com.io7m.azeno.error_codes.AzErrorCode;
import com.io7m.azeno.error_codes.AzStandardErrorCodes;
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.model.AzValidityException;
import com.io7m.azeno.protocol.asset.AzAResponseError;
import com.io7m.azeno.protocol.asset.cb.AzA1Messages;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctionalCoreAuthenticatedType;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctionalCoreType;
import com.io7m.azeno.server.http.AzHTTPRequestInformation;
import com.io7m.azeno.server.http.AzHTTPResponseFixedSize;
import com.io7m.azeno.server.http.AzHTTPResponseType;
import com.io7m.azeno.server.service.sessions.AzSession;
import com.io7m.azeno.server.service.sessions.AzSessionSecretIdentifier;
import com.io7m.azeno.server.service.sessions.AzSessionService;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import io.helidon.webserver.http.ServerRequest;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.io7m.azeno.protocol.asset.AzAResponseBlame.BLAME_CLIENT;
import static com.io7m.azeno.protocol.asset.AzAResponseBlame.BLAME_SERVER;
import static com.io7m.azeno.server.asset.v1.AzA1Errors.errorResponseOf;
import static com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType.setSpanErrorCode;
import static com.io7m.azeno.strings.AzStringConstants.ERROR_UNAUTHORIZED;

/**
 * A core that executes the given core under authentication.
 */

public final class AzA1HandlerCoreAuthenticated
  implements AzHTTPHandlerFunctionalCoreType
{
  private final AzHTTPHandlerFunctionalCoreAuthenticatedType<AzSession, AzUser> core;
  private final AzDatabaseType database;
  private final AzSessionService userSessions;
  private final AzA1Messages messages;
  private final AzStrings strings;

  private AzA1HandlerCoreAuthenticated(
    final RPServiceDirectoryType services,
    final AzHTTPHandlerFunctionalCoreAuthenticatedType<AzSession, AzUser> inCore)
  {
    Objects.requireNonNull(services, "services");

    this.core =
      Objects.requireNonNull(inCore, "core");
    this.strings =
      services.requireService(AzStrings.class);
    this.database =
      services.requireService(AzDatabaseType.class);
    this.userSessions =
      services.requireService(AzSessionService.class);
    this.messages =
      services.requireService(AzA1Messages.class);
  }

  /**
   * @param services The services
   * @param inCore   The executed core
   *
   * @return A core that executes the given core under authentication
   */

  public static AzHTTPHandlerFunctionalCoreType withAuthentication(
    final RPServiceDirectoryType services,
    final AzHTTPHandlerFunctionalCoreAuthenticatedType<AzSession, AzUser> inCore)
  {
    return new AzA1HandlerCoreAuthenticated(services, inCore);
  }

  @Override
  public AzHTTPResponseType execute(
    final ServerRequest request,
    final AzHTTPRequestInformation information)
  {
    final var headers =
      request.headers();
    final var cookies =
      headers.cookies();

    final String cookie;
    try {
      cookie = cookies.get("AZENO_ASSET_SESSION");
      Objects.requireNonNull(cookie, "cookie");
    } catch (final NoSuchElementException e) {
      return this.notAuthenticated(information);
    }

    final AzSessionSecretIdentifier userSessionId;
    try {
      userSessionId = new AzSessionSecretIdentifier(cookie);
    } catch (final AzValidityException e) {
      return this.notAuthenticated(information);
    }

    final var userSessionOpt =
      this.userSessions.findSession(userSessionId);

    if (userSessionOpt.isEmpty()) {
      return this.notAuthenticated(information);
    }

    final var userSession =
      userSessionOpt.get();

    final Optional<AzUser> userOpt;
    try {
      userOpt = this.userGet(userSession.userId());
    } catch (final DDatabaseException e) {
      setSpanErrorCode(new AzErrorCode(e.errorCode()));
      return errorResponseOf(this.messages, information, BLAME_SERVER, e);
    }

    if (userOpt.isEmpty()) {
      return this.notAuthenticated(information);
    }

    return this.core.executeAuthenticated(
      request,
      information,
      userSession,
      userOpt.get()
    );
  }

  private AzHTTPResponseType notAuthenticated(
    final AzHTTPRequestInformation information)
  {
    return new AzHTTPResponseFixedSize(
      401,
      Set.of(),
      AzA1Messages.contentType(),
      this.messages.serialize(
        new AzAResponseError(
          information.requestID(),
          this.strings.format(ERROR_UNAUTHORIZED),
          AzStandardErrorCodes.errorAuthentication(),
          Map.of(),
          Optional.empty(),
          Optional.empty(),
          BLAME_CLIENT,
          List.of()
        )
      )
    );
  }

  private Optional<AzUser> userGet(
    final AzUserID id)
    throws DDatabaseException
  {
    try (var c = this.database.openConnection()) {
      try (var t = c.openTransaction()) {
        final var q = t.query(AzUserGetType.class);
        return q.execute(id);
      }
    }
  }
}
