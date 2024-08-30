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

import com.io7m.azeno.error_codes.AzErrorCode;
import com.io7m.azeno.error_codes.AzException;
import com.io7m.azeno.error_codes.AzStandardErrorCodes;
import com.io7m.azeno.protocol.asset.AzAResponseBlame;
import com.io7m.azeno.protocol.asset.AzAResponseError;
import com.io7m.azeno.protocol.asset.AzATransactionResponse;
import com.io7m.azeno.protocol.asset.cb.AzA1Messages;
import com.io7m.azeno.server.controller.command_exec.AzCommandExecutionFailure;
import com.io7m.azeno.server.http.AzHTTPRequestInformation;
import com.io7m.azeno.server.http.AzHTTPResponseFixedSize;
import com.io7m.azeno.server.http.AzHTTPResponseType;
import com.io7m.darco.api.DDatabaseException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Functions to transform exceptions.
 */

public final class AzA1Errors
{
  private AzA1Errors()
  {

  }

  /**
   * Transform an exception into an error response.
   *
   * @param information The request information
   * @param blame       The blame assignment
   * @param exception   The exception
   *
   * @return An error response
   */

  static AzAResponseError errorOf(
    final AzHTTPRequestInformation information,
    final AzAResponseBlame blame,
    final AzException exception)
  {
    return new AzAResponseError(
      information.requestID(),
      exception.getMessage(),
      exception.errorCode(),
      exception.attributes(),
      exception.remediatingAction(),
      Optional.of(exception),
      blame,
      List.of()
    );
  }

  /**
   * Transform an exception into an error response.
   *
   * @param information The request information
   * @param blame       The blame assignment
   * @param exception   The exception
   *
   * @return An error response
   */

  static AzAResponseError errorOf(
    final AzHTTPRequestInformation information,
    final AzAResponseBlame blame,
    final DDatabaseException exception)
  {
    return new AzAResponseError(
      information.requestID(),
      exception.getMessage(),
      new AzErrorCode(exception.errorCode()),
      exception.attributes(),
      exception.remediatingAction(),
      Optional.of(exception),
      blame,
      List.of()
    );
  }

  /**
   * Transform an exception into an error response.
   *
   * @param information The request information
   * @param blame       The blame assignment
   * @param exception   The exception
   *
   * @return An error response
   */

  static AzAResponseError errorOf(
    final AzHTTPRequestInformation information,
    final AzAResponseBlame blame,
    final IOException exception)
  {
    return new AzAResponseError(
      information.requestID(),
      Objects.requireNonNullElse(exception.getMessage(), exception.getClass().getName()),
      AzStandardErrorCodes.errorIo(),
      Map.of(),
      Optional.empty(),
      Optional.of(exception),
      blame,
      List.of()
    );
  }

  /**
   * Transform an exception into an error response.
   *
   * @param messages    A message serializer
   * @param information The request information
   * @param blame       The blame assignment
   * @param exception   The exception
   *
   * @return An error response
   */

  public static AzHTTPResponseType errorResponseOf(
    final AzA1Messages messages,
    final AzHTTPRequestInformation information,
    final AzAResponseBlame blame,
    final AzException exception)
  {
    return new AzHTTPResponseFixedSize(
      switch (blame) {
        case BLAME_CLIENT -> 400;
        case BLAME_SERVER -> 500;
      },
      Set.of(),
      AzA1Messages.contentType(),
      messages.serialize(errorOf(information, blame, exception))
    );
  }

  /**
   * Transform an exception into an error response.
   *
   * @param messages    A message serializer
   * @param information The request information
   * @param blame       The blame assignment
   * @param exception   The exception
   *
   * @return An error response
   */

  public static AzHTTPResponseType errorResponseOf(
    final AzA1Messages messages,
    final AzHTTPRequestInformation information,
    final AzAResponseBlame blame,
    final DDatabaseException exception)
  {
    return new AzHTTPResponseFixedSize(
      switch (blame) {
        case BLAME_CLIENT -> 400;
        case BLAME_SERVER -> 500;
      },
      Set.of(),
      AzA1Messages.contentType(),
      messages.serialize(errorOf(information, blame, exception))
    );
  }

  /**
   * Transform an exception into an error response.
   *
   * @param messages    A message serializer
   * @param information The request information
   * @param exception   The exception
   *
   * @return An error response
   */

  public static AzHTTPResponseType errorResponseOf(
    final AzA1Messages messages,
    final AzHTTPRequestInformation information,
    final AzCommandExecutionFailure exception)
  {
    final AzAResponseBlame blame;
    if (exception.httpStatusCode() < 500) {
      blame = AzAResponseBlame.BLAME_CLIENT;
    } else {
      blame = AzAResponseBlame.BLAME_SERVER;
    }

    return new AzHTTPResponseFixedSize(
      exception.httpStatusCode(),
      Set.of(),
      AzA1Messages.contentType(),
      messages.serialize(errorOf(information, blame, exception))
    );
  }

  /**
   * Produce a transaction response type.
   *
   * @param messages A message serializer
   * @param response The transaction response
   *
   * @return An error response
   */

  public static AzHTTPResponseType transactionResponseOf(
    final AzA1Messages messages,
    final AzATransactionResponse response)
  {
    return new AzHTTPResponseFixedSize(
      response.firstError()
        .map(e -> {
          return switch (e.blame()) {
            case BLAME_CLIENT -> Integer.valueOf(400);
            case BLAME_SERVER -> Integer.valueOf(500);
          };
        })
        .orElseGet(() -> Integer.valueOf(200))
        .intValue(),
      Set.of(),
      AzA1Messages.contentTypeForSequence(),
      messages.serialize(response)
    );
  }
}
