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

import com.io7m.azeno.database.api.AzUserGetType;
import com.io7m.azeno.protocol.asset.AzACommandRolesGet;
import com.io7m.azeno.protocol.asset.AzAResponseRolesGet;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.azeno.server.controller.command_exec.AzCommandExecutionFailure;
import com.io7m.darco.api.DDatabaseException;

import java.util.Map;

import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorNonexistent;
import static com.io7m.azeno.strings.AzStringConstants.ERROR_NONEXISTENT;
import static com.io7m.azeno.strings.AzStringConstants.USER_ID;

/**
 * @see AzACommandRolesGet
 */

public final class AzACmdRolesGet extends AzACmdAbstract<AzACommandRolesGet>
{
  /**
   * @see AzACommandRolesGet
   */

  public AzACmdRolesGet()
  {

  }

  @Override
  protected AzAResponseType executeActual(
    final AzACommandContext context,
    final AzACommandRolesGet command)
    throws DDatabaseException, AzCommandExecutionFailure
  {
    final var get =
      context.transaction()
        .query(AzUserGetType.class);

    final var targetUser =
      get.execute(command.user())
        .orElseThrow(() -> {
          return context.failFormatted(
            400,
            errorNonexistent(),
            Map.of(USER_ID, command.user().toString()),
            ERROR_NONEXISTENT
          );
        });

    return new AzAResponseRolesGet(
      context.requestId(),
      targetUser.subject().roles()
    );
  }
}
