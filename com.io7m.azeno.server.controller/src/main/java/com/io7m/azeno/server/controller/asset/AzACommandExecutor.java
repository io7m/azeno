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

package com.io7m.azeno.server.controller.asset;

import com.io7m.azeno.protocol.asset.AzACommandAuditSearchBegin;
import com.io7m.azeno.protocol.asset.AzACommandAuditSearchNext;
import com.io7m.azeno.protocol.asset.AzACommandAuditSearchPrevious;
import com.io7m.azeno.protocol.asset.AzACommandDebugInvalid;
import com.io7m.azeno.protocol.asset.AzACommandDebugRandom;
import com.io7m.azeno.protocol.asset.AzACommandLogin;
import com.io7m.azeno.protocol.asset.AzACommandRolesAssign;
import com.io7m.azeno.protocol.asset.AzACommandRolesGet;
import com.io7m.azeno.protocol.asset.AzACommandRolesRevoke;
import com.io7m.azeno.protocol.asset.AzACommandType;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.azeno.server.controller.command_exec.AzCommandExecutionFailure;
import com.io7m.azeno.server.controller.command_exec.AzCommandExecutorType;

/**
 * A command executor for public commands.
 */

public final class AzACommandExecutor
  implements AzCommandExecutorType<
  AzACommandContext,
  AzACommandType<? extends AzAResponseType>,
  AzAResponseType>
{
  /**
   * A command executor for public commands.
   */

  public AzACommandExecutor()
  {

  }

  @Override
  public AzAResponseType execute(
    final AzACommandContext context,
    final AzACommandType<? extends AzAResponseType> command)
    throws AzCommandExecutionFailure
  {
    final var span =
      context.tracer()
        .spanBuilder(command.getClass().getSimpleName())
        .startSpan();

    try (var ignored = span.makeCurrent()) {
      context.transaction().setUserID(context.session().userId());
      return executeCommand(context, command);
    } catch (final Throwable e) {
      span.recordException(e);
      throw e;
    } finally {
      span.end();
    }
  }

  private static AzAResponseType executeCommand(
    final AzACommandContext context,
    final AzACommandType<? extends AzAResponseType> command)
    throws AzCommandExecutionFailure
  {
    return switch (command) {
      case final AzACommandRolesAssign m -> {
        yield new AzACmdRolesAssign().execute(context, m);
      }
      case final AzACommandRolesRevoke m -> {
        yield new AzACmdRolesRevoke().execute(context, m);
      }
      case final AzACommandRolesGet m -> {
        yield new AzACmdRolesGet().execute(context, m);
      }
      case final AzACommandAuditSearchBegin m -> {
        yield new AzACmdAuditSearchBegin().execute(context, m);
      }
      case final AzACommandAuditSearchNext m -> {
        yield new AzACmdAuditSearchNext().execute(context, m);
      }
      case final AzACommandAuditSearchPrevious m -> {
        yield new AzACmdAuditSearchPrevious().execute(context, m);
      }
      case final AzACommandDebugInvalid m -> {
        throw new IllegalStateException();
      }
      case final AzACommandDebugRandom m -> {
        throw new IllegalStateException();
      }
      case final AzACommandLogin m -> {
        throw new IllegalStateException();
      }
    };
  }
}
