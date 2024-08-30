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

import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.error_codes.AzErrorCode;
import com.io7m.azeno.error_codes.AzStandardErrorCodes;
import com.io7m.azeno.protocol.api.AzProtocolException;
import com.io7m.azeno.protocol.asset.AzACommandType;
import com.io7m.azeno.protocol.asset.AzAMessageType;
import com.io7m.azeno.protocol.asset.AzAResponseError;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.azeno.protocol.asset.cb.AzA1Messages;
import com.io7m.azeno.server.controller.asset.AzACommandContext;
import com.io7m.azeno.server.controller.asset.AzACommandExecutor;
import com.io7m.azeno.server.controller.command_exec.AzCommandExecutionFailure;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctional;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctionalCoreType;
import com.io7m.azeno.server.http.AzHTTPRequestInformation;
import com.io7m.azeno.server.http.AzHTTPResponseFixedSize;
import com.io7m.azeno.server.http.AzHTTPResponseType;
import com.io7m.azeno.server.service.reqlimit.AzRequestLimitExceeded;
import com.io7m.azeno.server.service.reqlimit.AzRequestLimits;
import com.io7m.azeno.server.service.sessions.AzSession;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import io.helidon.webserver.http.ServerRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.io7m.azeno.protocol.asset.AzAResponseBlame.BLAME_CLIENT;
import static com.io7m.azeno.protocol.asset.AzAResponseBlame.BLAME_SERVER;
import static com.io7m.azeno.server.asset.v1.AzA1Errors.errorResponseOf;
import static com.io7m.azeno.server.asset.v1.AzA1HandlerCoreAuthenticated.withAuthentication;
import static com.io7m.azeno.server.asset.v1.AzA1HandlerCoreTransactional.withTransaction;
import static com.io7m.azeno.server.http.AzHTTPHandlerCoreInstrumented.withInstrumentation;
import static com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType.setSpanErrorCode;
import static com.io7m.azeno.strings.AzStringConstants.ERROR_COMMAND_NOT_HERE;

/**
 * The schema_v1 command servlet.
 */

public final class AzA1HandlerCommand extends AzHTTPHandlerFunctional
{
  /**
   * The schema_v1 command servlet.
   *
   * @param services The services
   */

  public AzA1HandlerCommand(
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
    final var telemetry =
      services.requireService(AzServerTelemetryServiceType.class);

    final var authenticated =
      withAuthentication(services, (req1, info1, session, user) -> {
        return withTransaction(services, (req2, info2, transaction) -> {
          return execute(
            services,
            req2,
            info2,
            messages,
            telemetry,
            limits,
            strings,
            session,
            transaction
          );
        }).execute(req1, info1);
      });

    return withInstrumentation(services, authenticated);
  }

  private static AzHTTPResponseType execute(
    final RPServiceDirectoryType services,
    final ServerRequest request,
    final AzHTTPRequestInformation information,
    final AzA1Messages messages,
    final AzServerTelemetryServiceType telemetry,
    final AzRequestLimits limits,
    final AzStrings strings,
    final AzSession session,
    final AzDatabaseTransactionType transaction)
  {
    try (var input = limits.boundedMaximumInput(request, 1048576L)) {
      final var message =
        parseMessage(telemetry, messages, input);

      if (message instanceof final AzACommandType<?> command) {
        return executeCommand(
          services,
          information,
          messages,
          telemetry,
          session,
          command,
          transaction
        );
      }

      return errorResponseOf(
        messages,
        information,
        BLAME_CLIENT,
        new AzProtocolException(
          strings.format(ERROR_COMMAND_NOT_HERE),
          AzStandardErrorCodes.errorApiMisuse(),
          Map.of(),
          Optional.empty()
        )
      );

    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    } catch (final AzRequestLimitExceeded | AzProtocolException e) {
      setSpanErrorCode(e.errorCode());
      return errorResponseOf(messages, information, BLAME_CLIENT, e);
    } catch (final DDatabaseException e) {
      setSpanErrorCode(new AzErrorCode(e.errorCode()));
      return errorResponseOf(messages, information, BLAME_SERVER, e);
    }
  }

  private static AzAMessageType parseMessage(
    final AzServerTelemetryServiceType telemetry,
    final AzA1Messages messages,
    final InputStream input)
    throws IOException, AzProtocolException
  {
    final var parseSpan =
      telemetry.tracer()
        .spanBuilder("ParseMessage")
        .startSpan();

    try (var ignored = parseSpan.makeCurrent()) {
      final var data = parseMessageReadData(telemetry, input);
      return parseMessageDeserialize(telemetry, messages, data);
    } finally {
      parseSpan.end();
    }
  }

  private static AzAMessageType parseMessageDeserialize(
    final AzServerTelemetryServiceType telemetry,
    final AzA1Messages messages,
    final byte[] data)
    throws AzProtocolException
  {
    final var readSpan =
      telemetry.tracer()
        .spanBuilder("Deserialize")
        .startSpan();

    try (var ignored = readSpan.makeCurrent()) {
      return messages.parse(data);
    } finally {
      readSpan.end();
    }
  }

  private static byte[] parseMessageReadData(
    final AzServerTelemetryServiceType telemetry,
    final InputStream input)
    throws IOException
  {
    final var readSpan =
      telemetry.tracer()
        .spanBuilder("Read")
        .startSpan();

    try (var ignored = readSpan.makeCurrent()) {
      return input.readAllBytes();
    } finally {
      readSpan.end();
    }
  }

  private static AzHTTPResponseType executeCommand(
    final RPServiceDirectoryType services,
    final AzHTTPRequestInformation information,
    final AzA1Messages messages,
    final AzServerTelemetryServiceType telemetry,
    final AzSession session,
    final AzACommandType<?> command,
    final AzDatabaseTransactionType transaction)
    throws DDatabaseException
  {
    final var executor =
      new AzACommandExecutor();

    final var context =
      new AzACommandContext(
        services,
        information.requestID(),
        transaction,
        session,
        information.remoteAddress(),
        information.userAgent()
      );

    final AzAResponseType result;
    try {
      result = executor.execute(context, command);
    } catch (final AzCommandExecutionFailure e) {
      setSpanErrorCode(e.errorCode());
      return errorResponseOf(messages, information, e);
    }

    if (result instanceof final AzAResponseError error) {
      setSpanErrorCode(error.errorCode());
      return new AzHTTPResponseFixedSize(
        switch (error.blame()) {
          case BLAME_SERVER -> 500;
          case BLAME_CLIENT -> 400;
        },
        Set.of(),
        AzA1Messages.contentType(),
        messages.serialize(error)
      );
    }

    commit(telemetry, transaction);
    return new AzHTTPResponseFixedSize(
      200,
      Set.of(),
      AzA1Messages.contentType(),
      messages.serialize(result)
    );
  }

  private static void commit(
    final AzServerTelemetryServiceType telemetry,
    final AzDatabaseTransactionType transaction)
    throws DDatabaseException
  {
    final var commitSpan =
      telemetry.tracer()
        .spanBuilder("Commit")
        .startSpan();

    try (var ignored = commitSpan.makeCurrent()) {
      transaction.commit();
    } finally {
      commitSpan.end();
    }
  }
}
