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

import com.io7m.azeno.error_codes.AzErrorCode;
import com.io7m.azeno.error_codes.AzException;
import com.io7m.azeno.protocol.asset.AzAResponseBlame;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A failure to execute a command.
 */

public final class AzCommandExecutionFailure extends AzException
{
  private final UUID requestId;
  private final int httpStatusCode;

  /**
   * @return The request ID
   */

  public UUID requestId()
  {
    return this.requestId;
  }

  /**
   * @return The HTTP status kind
   */

  public int httpStatusCode()
  {
    return this.httpStatusCode;
  }

  /**
   * @return The inferred blame from the exception
   */

  public AzAResponseBlame blame()
  {
    if (this.httpStatusCode >= 500 && this.httpStatusCode < 600) {
      return AzAResponseBlame.BLAME_SERVER;
    }
    return AzAResponseBlame.BLAME_CLIENT;
  }

  /**
   * Construct an exception.
   *
   * @param message             The message
   * @param inErrorCode         The error kind
   * @param inAttributes        The error attributes
   * @param inRemediatingAction The remediating action, if any
   * @param inRequestId         The request ID
   * @param inHttpStatusCode    The HTTP status kind
   */

  public AzCommandExecutionFailure(
    final String message,
    final AzErrorCode inErrorCode,
    final Map<String, String> inAttributes,
    final Optional<String> inRemediatingAction,
    final UUID inRequestId,
    final int inHttpStatusCode)
  {
    super(message, inErrorCode, inAttributes, inRemediatingAction);
    this.requestId =
      Objects.requireNonNull(inRequestId, "requestId");
    this.httpStatusCode =
      inHttpStatusCode;
  }

  /**
   * Construct an exception.
   *
   * @param message             The message
   * @param cause               The cause
   * @param inErrorCode         The error kind
   * @param inAttributes        The error attributes
   * @param inRemediatingAction The remediating action, if any
   * @param inRequestId         The request ID
   * @param inHttpStatusCode    The HTTP status kind
   */

  public AzCommandExecutionFailure(
    final String message,
    final Throwable cause,
    final AzErrorCode inErrorCode,
    final Map<String, String> inAttributes,
    final Optional<String> inRemediatingAction,
    final UUID inRequestId,
    final int inHttpStatusCode)
  {
    super(message, cause, inErrorCode, inAttributes, inRemediatingAction);
    this.requestId =
      Objects.requireNonNull(inRequestId, "requestId");
    this.httpStatusCode =
      inHttpStatusCode;
  }
}
