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

import com.io7m.azeno.error_codes.AzException;
import com.io7m.azeno.model.AzValidityException;
import com.io7m.azeno.protocol.api.AzProtocolException;
import com.io7m.azeno.protocol.api.AzProtocolMessageType;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.azeno.security.AzSecurityException;
import com.io7m.azeno.server.controller.command_exec.AzCommandExecutionFailure;
import com.io7m.azeno.server.controller.command_exec.AzCommandExecutorType;
import com.io7m.darco.api.DDatabaseException;

import java.util.Objects;

/**
 * The abstract base command class.
 *
 * @param <C> The type of accepted commands
 */

public abstract class AzACmdAbstract<C extends AzProtocolMessageType>
  implements AzCommandExecutorType<AzACommandContext, C, AzAResponseType>
{
  protected AzACmdAbstract()
  {

  }

  @Override
  public final AzAResponseType execute(
    final AzACommandContext context,
    final C command)
    throws AzCommandExecutionFailure
  {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(command, "command");

    try {
      return this.executeActual(context, command);
    } catch (final AzValidityException e) {
      throw context.failValidity(e);
    } catch (final AzSecurityException e) {
      throw context.failSecurity(e);
    } catch (final DDatabaseException e) {
      throw context.failDatabase(e);
    } catch (final AzProtocolException e) {
      throw context.failProtocol(e);
    } catch (final AzCommandExecutionFailure e) {
      throw context.failWithCause(
        e,
        e.httpStatusCode(),
        e.errorCode(),
        e.getMessage(),
        e.attributes()
      );
    } catch (final AzException e) {
      throw context.failWithCause(
        e,
        500,
        e.errorCode(),
        e.getMessage(),
        e.attributes()
      );
    }
  }

  protected abstract AzAResponseType executeActual(
    AzACommandContext context,
    C command)
    throws
    AzValidityException,
    AzException,
    DDatabaseException;
}
