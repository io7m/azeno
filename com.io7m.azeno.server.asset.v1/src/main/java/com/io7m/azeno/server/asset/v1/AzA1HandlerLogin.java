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

import com.io7m.azeno.database.api.AzAuditPutType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.database.api.AzDatabaseType;
import com.io7m.azeno.database.api.AzDatabaseUserUpdates;
import com.io7m.azeno.error_codes.AzException;
import com.io7m.azeno.error_codes.AzStandardErrorCodes;
import com.io7m.azeno.model.AzAuditEvent;
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.protocol.asset.AzACommandLogin;
import com.io7m.azeno.protocol.asset.AzAResponseBlame;
import com.io7m.azeno.protocol.asset.AzAResponseLogin;
import com.io7m.azeno.protocol.asset.cb.AzA1Messages;
import com.io7m.azeno.server.http.AzHTTPCookieDeclaration;
import com.io7m.azeno.server.http.AzHTTPErrorStatusException;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctional;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctionalCoreType;
import com.io7m.azeno.server.http.AzHTTPRequestInformation;
import com.io7m.azeno.server.http.AzHTTPResponseFixedSize;
import com.io7m.azeno.server.http.AzHTTPResponseType;
import com.io7m.azeno.server.service.configuration.AzConfigurationServiceType;
import com.io7m.azeno.server.service.idstore.AzIdstoreClientsType;
import com.io7m.azeno.server.service.reqlimit.AzRequestLimits;
import com.io7m.azeno.server.service.sessions.AzSessionService;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.idstore.model.IdLoginMetadataStandard;
import com.io7m.idstore.protocol.user.IdUResponseLogin;
import com.io7m.idstore.user_client.api.IdUClientConnectionParameters;
import com.io7m.idstore.user_client.api.IdUClientException;
import com.io7m.medrina.api.MSubject;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import io.helidon.webserver.http.ServerRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorApiMisuse;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorProtocol;
import static com.io7m.azeno.server.http.AzHTTPHandlerCoreInstrumented.withInstrumentation;
import static com.io7m.azeno.strings.AzStringConstants.COMMAND;
import static com.io7m.azeno.strings.AzStringConstants.ERROR_EXPECTED_COMMAND;
import static io.helidon.http.Status.BAD_REQUEST_400;
import static java.util.Map.entry;

/**
 * The schema_v1 login servlet.
 */

public final class AzA1HandlerLogin extends AzHTTPHandlerFunctional
{
  private static final Duration DEFAULT_EXPIRATION =
    Duration.ofDays(365L);

  /**
   * The schema_v1 login servlet.
   *
   * @param services The services
   */

  public AzA1HandlerLogin(
    final RPServiceDirectoryType services)
  {
    super(createCore(services));
  }

  private static AzHTTPHandlerFunctionalCoreType createCore(
    final RPServiceDirectoryType services)
  {
    final var limits =
      services.requireService(AzRequestLimits.class);
    final var messages =
      services.requireService(AzA1Messages.class);
    final var strings =
      services.requireService(AzStrings.class);
    final var database =
      services.requireService(AzDatabaseType.class);
    final var sessions =
      services.requireService(AzSessionService.class);
    final var telemetry =
      services.requireService(AzServerTelemetryServiceType.class);
    final var idClients =
      services.requireService(AzIdstoreClientsType.class);
    final var configuration =
      services.requireService(AzConfigurationServiceType.class);

    final var sessionDuration =
      configuration.configuration()
        .assetApiConfiguration()
        .sessionExpiration()
        .orElse(DEFAULT_EXPIRATION);

    final var transactional =
      AzA1HandlerCoreTransactional.withTransaction(services, (request, info, transaction) -> {
        return execute(
          database,
          telemetry,
          idClients,
          sessions,
          strings,
          limits,
          messages,
          request,
          info,
          transaction,
          sessionDuration
        );
      });

    return withInstrumentation(services, transactional);
  }

  private static AzHTTPResponseType execute(
    final AzDatabaseType database,
    final AzServerTelemetryServiceType telemetry,
    final AzIdstoreClientsType idClients,
    final AzSessionService sessions,
    final AzStrings strings,
    final AzRequestLimits limits,
    final AzA1Messages messages,
    final ServerRequest request,
    final AzHTTPRequestInformation information,
    final AzDatabaseTransactionType transaction,
    final Duration sessionDuration)
  {
    final AzACommandLogin login;
    try {
      login = readLoginCommand(strings, limits, messages, request);
    } catch (final AzException e) {
      return AzA1Errors.errorResponseOf(
        messages,
        information,
        AzAResponseBlame.BLAME_CLIENT,
        e);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    /*
     * Authenticate with idstore.
     */

    final UUID userId;
    final var clientMetadata =
      Map.ofEntries(
        entry(
          IdLoginMetadataStandard.remoteHostProxied(),
          information.remoteAddress()
        )
      );

    final var clientCredentials =
      new IdUClientConnectionParameters(
        login.userName().value(),
        login.password(),
        idClients.baseURI(),
        clientMetadata,
        Duration.ofSeconds(30L),
        Duration.ofSeconds(30L)
      );

    final var span =
      telemetry.tracer()
        .spanBuilder("IdstoreLogin")
        .startSpan();

    try (var ignored = span.makeCurrent()) {
      try (var client = idClients.createClient()) {
        final var result =
          client.connectOrThrow(clientCredentials);
        final var resultM =
          (IdUResponseLogin) result;

        userId = resultM.user().id();
      } catch (final IdUClientException e) {
        span.setAttribute("idstore.errorCode", e.errorCode().id());
        return AzA1Errors.errorResponseOf(
          messages,
          information,
          AzAResponseBlame.BLAME_CLIENT,
          new AzException(
            e.getMessage(),
            e,
            AzStandardErrorCodes.errorAuthentication(),
            e.attributes(),
            e.remediatingAction()
          )
        );
      } catch (final InterruptedException e) {
        throw new IllegalStateException(e);
      }
    } finally {
      span.end();
    }

    /*
     * Merge in the user's latest username and credentials.
     */

    var icUser =
      new AzUser(
        new AzUserID(userId),
        login.userName(),
        new MSubject(Set.of()));

    try {
      icUser = AzDatabaseUserUpdates.userMerge(database, icUser);
    } catch (final DDatabaseException e) {
      return AzA1Errors.errorResponseOf(
        messages,
        information,
        AzAResponseBlame.BLAME_SERVER,
        e);
    }

    return createNewSession(
      messages,
      sessions,
      information,
      transaction,
      login,
      icUser,
      sessionDuration
    );
  }


  /**
   * Create a new session.
   */

  private static AzHTTPResponseType createNewSession(
    final AzA1Messages messages,
    final AzSessionService sessions,
    final AzHTTPRequestInformation information,
    final AzDatabaseTransactionType transaction,
    final AzACommandLogin login,
    final AzUser icUser,
    final Duration sessionDuration)
  {
    final var session =
      sessions.createSession(
        icUser.userId(),
        login.userName(),
        icUser.subject()
      );

    try {
      transaction.query(AzAuditPutType.class)
        .execute(new AzAuditEvent(
          0L,
          OffsetDateTime.now(),
          icUser.userId(),
          "USER_LOGGED_IN",
          Map.ofEntries(
            entry("Host", information.remoteAddress()),
            entry("UserAgent", information.userAgent())
          )
        ));

      transaction.commit();
    } catch (final DDatabaseException e) {
      return AzA1Errors.errorResponseOf(
        messages,
        information,
        AzAResponseBlame.BLAME_SERVER,
        e);
    }

    final var cookie =
      new AzHTTPCookieDeclaration(
        "AZENO_ASSET_SESSION",
        session.id().value(),
        sessionDuration
      );

    return new AzHTTPResponseFixedSize(
      200,
      Set.of(cookie),
      AzA1Messages.contentType(),
      messages.serialize(
        new AzAResponseLogin(
          information.requestID(),
          icUser.userId()
        )
      )
    );
  }

  private static AzACommandLogin readLoginCommand(
    final AzStrings strings,
    final AzRequestLimits limits,
    final AzA1Messages messages,
    final ServerRequest request)
    throws AzHTTPErrorStatusException, IOException
  {
    try (var input = limits.boundedMaximumInputForLoginCommand(request)) {
      final var data = input.readAllBytes();
      final var message = messages.parse(data);
      if (message instanceof final AzACommandLogin login) {
        return login;
      }
    } catch (final AzException e) {
      throw new AzHTTPErrorStatusException(
        e.getMessage(),
        e,
        errorProtocol(),
        e.attributes(),
        e.remediatingAction(),
        BAD_REQUEST_400.code()
      );
    }

    throw new AzHTTPErrorStatusException(
      strings.format(ERROR_EXPECTED_COMMAND),
      errorApiMisuse(),
      Map.of(
        strings.format(COMMAND), "CommandLogin"
      ),
      Optional.empty(),
      BAD_REQUEST_400.code()
    );
  }
}
