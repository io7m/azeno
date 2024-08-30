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
import com.io7m.azeno.protocol.asset.AzATransactionResponse;
import com.io7m.azeno.protocol.asset.cb.AzA1Messages;
import com.io7m.azeno.server.controller.asset.AzACommandContext;
import com.io7m.azeno.server.controller.asset.AzACommandExecutor;
import com.io7m.azeno.server.controller.command_exec.AzCommandExecutionFailure;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctional;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctionalCoreType;
import com.io7m.azeno.server.http.AzHTTPRequestInformation;
import com.io7m.azeno.server.http.AzHTTPResponseType;
import com.io7m.azeno.server.service.reqlimit.AzRequestLimitExceeded;
import com.io7m.azeno.server.service.reqlimit.AzRequestLimits;
import com.io7m.azeno.server.service.sessions.AzSession;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType;
import com.io7m.azeno.strings.AzStringConstants;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import io.helidon.webserver.http.ServerRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.io7m.azeno.protocol.asset.AzAResponseBlame.BLAME_CLIENT;
import static com.io7m.azeno.protocol.asset.AzAResponseBlame.BLAME_SERVER;
import static com.io7m.azeno.server.asset.v1.AzA1Errors.errorOf;
import static com.io7m.azeno.server.asset.v1.AzA1Errors.transactionResponseOf;
import static com.io7m.azeno.server.http.AzHTTPHandlerCoreInstrumented.withInstrumentation;
import static com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType.setSpanErrorCode;
import static com.io7m.azeno.strings.AzStringConstants.ERROR_COMMAND_NOT_HERE;

/**
 * The schema_v1 transaction servlet.
 */

public final class AzA1HandlerTransaction extends AzHTTPHandlerFunctional
{
  /**
   * The schema_v1 transaction servlet.
   *
   * @param services The services
   */

  public AzA1HandlerTransaction(
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
      AzA1HandlerCoreAuthenticated.withAuthentication(services, (req1, info1, session, user) -> {
        return AzA1HandlerCoreTransactional.withTransaction(services, (req2, info2, transaction) -> {
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
    final var results = new ArrayList<AzAResponseType>(16);
    try (var input = limits.boundedMaximumInput(request, 1048576L)) {
      final var messagesParsed =
        parseMessages(telemetry, strings, messages, input);

      for (final var message : messagesParsed) {
        if (message instanceof final AzACommandType<?> command) {
          final var r =
            executeCommand(
              services,
              information,
              session,
              command,
              transaction
            );

          results.add(r);
          if (r instanceof AzAResponseError) {
            return respond(messages, results);
          }
          continue;
        }

        results.add(errorOf(
          information,
          BLAME_CLIENT,
          new AzProtocolException(
            strings.format(ERROR_COMMAND_NOT_HERE),
            AzStandardErrorCodes.errorApiMisuse(),
            Map.of(),
            Optional.empty()
          )));

        return respond(messages, results);
      }

      commit(telemetry, transaction);
      return respond(messages, results);
    } catch (final IOException e) {
      setSpanErrorCode(AzStandardErrorCodes.errorIo());
      results.add(errorOf(information, BLAME_SERVER, e));
      return respond(messages, results);
    } catch (final AzRequestLimitExceeded | AzProtocolException e) {
      setSpanErrorCode(e.errorCode());
      results.add(errorOf(information, BLAME_CLIENT, e));
      return respond(messages, results);
    } catch (final DDatabaseException e) {
      setSpanErrorCode(new AzErrorCode(e.errorCode()));
      results.add(errorOf(information, BLAME_SERVER, e));
      return respond(messages, results);
    }
  }

  private static AzHTTPResponseType respond(
    final AzA1Messages messages,
    final ArrayList<AzAResponseType> results)
  {
    return transactionResponseOf(
      messages,
      new AzATransactionResponse(results)
    );
  }

  private static List<AzAMessageType> parseMessages(
    final AzServerTelemetryServiceType telemetry,
    final AzStrings strings,
    final AzA1Messages messages,
    final InputStream input)
    throws IOException, AzProtocolException
  {
    final var parseSpan =
      telemetry.tracer()
        .spanBuilder("ParseMessages")
        .startSpan();

    try (var ignored = parseSpan.makeCurrent()) {
      final var data = parseMessageReadData(telemetry, strings, input);
      return parseMessagesDeserialize(telemetry, messages, data);
    } finally {
      parseSpan.end();
    }
  }

  private static List<AzAMessageType> parseMessagesDeserialize(
    final AzServerTelemetryServiceType telemetry,
    final AzA1Messages messages,
    final List<byte[]> data)
    throws AzProtocolException
  {
    final var readSpan =
      telemetry.tracer()
        .spanBuilder("Deserialize")
        .startSpan();

    final var results = new ArrayList<AzAMessageType>(data.size());
    try (var ignored = readSpan.makeCurrent()) {
      for (final var item : data) {
        results.add(messages.parse(item));
      }
      return List.copyOf(results);
    } finally {
      readSpan.end();
    }
  }

  private static List<byte[]> parseMessageReadData(
    final AzServerTelemetryServiceType telemetry,
    final AzStrings strings,
    final InputStream input)
    throws IOException, AzProtocolException
  {
    final var readSpan =
      telemetry.tracer()
        .spanBuilder("Read")
        .startSpan();

    final var results = new ArrayList<byte[]>();
    try (var ignored = readSpan.makeCurrent()) {
      while (true) {
        final var size =
          input.readNBytes(4);

        if (size.length != 4) {
          throw new AzProtocolException(
            strings.format(AzStringConstants.ERROR_IO),
            AzStandardErrorCodes.errorApiMisuse(),
            Map.of(),
            Optional.empty()
          );
        }

        final var sizeBuffer = ByteBuffer.wrap(size);
        sizeBuffer.order(ByteOrder.BIG_ENDIAN);

        final var messageSize = sizeBuffer.getInt(0);
        if (messageSize == 0) {
          return List.copyOf(results);
        }

        results.add(input.readNBytes(messageSize));
      }
    } finally {
      readSpan.end();
    }
  }

  private static AzAResponseType executeCommand(
    final RPServiceDirectoryType services,
    final AzHTTPRequestInformation information,
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

    try {
      return executor.execute(context, command);
    } catch (final AzCommandExecutionFailure e) {
      setSpanErrorCode(e.errorCode());
      return errorOf(information, e.blame(), e);
    }
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
