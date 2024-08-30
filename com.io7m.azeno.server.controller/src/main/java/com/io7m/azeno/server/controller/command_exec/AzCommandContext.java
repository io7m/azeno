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


package com.io7m.azeno.server.controller.command_exec;

import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.error_codes.AzErrorCode;
import com.io7m.azeno.error_codes.AzStandardErrorCodes;
import com.io7m.azeno.model.AzValidityException;
import com.io7m.azeno.protocol.api.AzProtocolException;
import com.io7m.azeno.protocol.api.AzProtocolMessageType;
import com.io7m.azeno.security.AzSecurityException;
import com.io7m.azeno.server.service.clock.AzServerClock;
import com.io7m.azeno.server.service.sessions.AzSession;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType;
import com.io7m.azeno.strings.AzStringConstantType;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import io.opentelemetry.api.trace.Tracer;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The context for execution of a command (or set of commands in a
 * transaction).
 *
 * @param <E> The type of error messages
 */

public abstract class AzCommandContext<E extends AzProtocolMessageType>
{
  private final RPServiceDirectoryType services;
  private final UUID requestId;
  private final AzDatabaseTransactionType transaction;
  private final AzServerClock clock;
  private final AzStrings strings;
  private final AzSession session;
  private final String remoteHost;
  private final String remoteUserAgent;
  private final Tracer tracer;
  private final Map<AzStringConstantType, String> attributes;

  /**
   * The context for execution of a command (or set of commands in a
   * transaction).
   *
   * @param inServices        The service directory
   * @param inRequestId       The request ID
   * @param inTransaction     The transaction
   * @param inSession         The user session
   * @param inRemoteHost      The remote host
   * @param inRemoteUserAgent The remote user agent
   */

  public AzCommandContext(
    final RPServiceDirectoryType inServices,
    final UUID inRequestId,
    final AzDatabaseTransactionType inTransaction,
    final AzSession inSession,
    final String inRemoteHost,
    final String inRemoteUserAgent)
  {
    this.services =
      Objects.requireNonNull(inServices, "services");
    this.requestId =
      Objects.requireNonNull(inRequestId, "requestId");
    this.transaction =
      Objects.requireNonNull(inTransaction, "transaction");
    this.session =
      Objects.requireNonNull(inSession, "inSession");
    this.remoteHost =
      Objects.requireNonNull(inRemoteHost, "remoteHost");
    this.remoteUserAgent =
      Objects.requireNonNull(inRemoteUserAgent, "remoteUserAgent");

    this.clock =
      inServices.requireService(AzServerClock.class);
    this.strings =
      inServices.requireService(AzStrings.class);
    this.tracer =
      inServices.requireService(AzServerTelemetryServiceType.class)
        .tracer();
    this.attributes =
      new HashMap<>(0);
  }

  /**
   * @return The attributes as an immutable map
   */

  public final Map<AzStringConstantType, String> attributes()
  {
    return Map.copyOf(this.attributes);
  }

  /**
   * Set an attribute for error reporting.
   *
   * @param key   The key string constant
   * @param value The value
   */

  public final void setAttribute(
    final AzStringConstantType key,
    final String value)
  {
    Objects.requireNonNull(key, "constant");
    Objects.requireNonNull(value, "value");
    this.attributes.put(key, value);
  }

  /**
   * @return The user session
   */

  public final AzSession session()
  {
    return this.session;
  }

  /**
   * @return The remote host
   */

  public final String remoteHost()
  {
    return this.remoteHost;
  }

  /**
   * @return The remote user agent
   */

  public final String remoteUserAgent()
  {
    return this.remoteUserAgent;
  }

  /**
   * @return The service directory used during execution
   */

  public final RPServiceDirectoryType services()
  {
    return this.services;
  }

  /**
   * @return The ID of the incoming request
   */

  public final UUID requestId()
  {
    return this.requestId;
  }

  /**
   * @return The database transaction
   */

  public final AzDatabaseTransactionType transaction()
  {
    return this.transaction;
  }

  /**
   * Localize the given string.
   *
   * @param constant The string
   * @param objects  The object arguments
   *
   * @return The localized string
   */

  public final String local(
    final AzStringConstantType constant,
    final Object... objects)
  {
    return this.strings.format(constant, objects);
  }

  /**
   * @return The OpenTelemetry tracer
   */

  public final Tracer tracer()
  {
    return this.tracer;
  }

  /**
   * @return The current time
   */

  public final OffsetDateTime now()
  {
    return this.clock.now();
  }

  /**
   * Produce an exception indicating an error, with a formatted error message.
   *
   * @param statusCode      The HTTP status kind
   * @param errorCode       The error kind
   * @param errorAttributes The error attributes
   * @param messageId       The string resource message ID
   * @param args            The string resource format arguments
   *
   * @return An execution failure
   */

  public final AzCommandExecutionFailure failFormatted(
    final int statusCode,
    final AzErrorCode errorCode,
    final Map<AzStringConstantType, String> errorAttributes,
    final AzStringConstantType messageId,
    final Object... args)
  {
    return this.fail(
      statusCode,
      errorCode,
      this.strings.format(messageId, args),
      errorAttributes
    );
  }

  /**
   * Produce an exception indicating an error, with a formatted error message.
   *
   * @param statusCode      The HTTP status kind
   * @param exception       The exception
   * @param errorCode       The error kind
   * @param errorAttributes The error attributes
   * @param messageId       The string resource message ID
   * @param args            The string resource format arguments
   *
   * @return An execution failure
   */

  public final AzCommandExecutionFailure failFormatted(
    final Exception exception,
    final int statusCode,
    final AzErrorCode errorCode,
    final Map<AzStringConstantType, String> errorAttributes,
    final AzStringConstantType messageId,
    final Object... args)
  {
    return new AzCommandExecutionFailure(
      this.strings.format(messageId, args),
      exception,
      errorCode,
      this.localMap(errorAttributes),
      Optional.empty(),
      this.requestId,
      statusCode
    );
  }

  private Map<String, String> localMap(
    final Map<AzStringConstantType, String> unformattedAttributes)
  {
    return unformattedAttributes.entrySet()
      .stream()
      .map(e -> Map.entry(this.local(e.getKey()), e.getValue()))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Produce an exception indicating an error, with a string constant message.
   *
   * @param statusCode      The HTTP status kind
   * @param errorCode       The error kind
   * @param errorAttributes The error attributes
   * @param message         The string message
   *
   * @return An execution failure
   */

  public final AzCommandExecutionFailure fail(
    final int statusCode,
    final AzErrorCode errorCode,
    final String message,
    final Map<AzStringConstantType, String> errorAttributes)
  {
    return new AzCommandExecutionFailure(
      message,
      errorCode,
      this.localMap(errorAttributes),
      Optional.empty(),
      this.requestId,
      statusCode
    );
  }

  /**
   * Produce an exception indicating an error, with a string constant message.
   *
   * @param statusCode      The HTTP status kind
   * @param errorCode       The error kind
   * @param errorAttributes The error attributes
   * @param message         The string message
   * @param cause           The cause
   *
   * @return An execution failure
   */

  public final AzCommandExecutionFailure failWithCause(
    final Exception cause,
    final int statusCode,
    final AzErrorCode errorCode,
    final String message,
    final Map<String, String> errorAttributes)
  {
    return new AzCommandExecutionFailure(
      message,
      cause,
      errorCode,
      errorAttributes,
      Optional.empty(),
      this.requestId,
      statusCode
    );
  }

  /**
   * Produce an exception indicating a database error.
   *
   * @param e The database exception
   *
   * @return An execution failure
   */

  public final AzCommandExecutionFailure failDatabase(
    final DDatabaseException e)
  {
    return new AzCommandExecutionFailure(
      e.getMessage(),
      e,
      new AzErrorCode(e.errorCode()),
      e.attributes(),
      Optional.empty(),
      this.requestId,
      500
    );
  }

  /**
   * Produce an exception indicating a security policy error.
   *
   * @param e The security exception
   *
   * @return An execution failure
   */

  public AzCommandExecutionFailure failSecurity(
    final AzSecurityException e)
  {
    return new AzCommandExecutionFailure(
      e.getMessage(),
      e,
      AzStandardErrorCodes.errorSecurityPolicyDenied(),
      e.attributes(),
      Optional.empty(),
      this.requestId,
      400
    );
  }

  /**
   * Produce an exception indicating a protocol error.
   *
   * @param e The exception
   *
   * @return An execution failure
   */

  public AzCommandExecutionFailure failProtocol(
    final AzProtocolException e)
  {
    return new AzCommandExecutionFailure(
      e.getMessage(),
      e,
      AzStandardErrorCodes.errorProtocol(),
      e.attributes(),
      Optional.empty(),
      this.requestId,
      400
    );
  }

  /**
   * Produce an exception indicating a validation error.
   *
   * @param e The exception
   *
   * @return An execution failure
   */

  public AzCommandExecutionFailure failValidity(
    final AzValidityException e)
  {
    return new AzCommandExecutionFailure(
      e.getMessage(),
      e,
      AzStandardErrorCodes.errorProtocol(),
      Map.of(),
      Optional.empty(),
      this.requestId,
      400
    );
  }
}
