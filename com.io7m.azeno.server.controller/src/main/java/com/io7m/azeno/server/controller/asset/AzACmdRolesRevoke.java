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
import com.io7m.azeno.database.api.AzUserPutType;
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.protocol.asset.AzACommandRolesRevoke;
import com.io7m.azeno.protocol.asset.AzAResponseOK;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.azeno.server.controller.command_exec.AzCommandExecutionFailure;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.medrina.api.MRoleName;
import com.io7m.medrina.api.MSubject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorNonexistent;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorOperationNotPermitted;
import static com.io7m.azeno.security.AzSecurityPolicy.ROLE_ASSET_ADMIN;
import static com.io7m.azeno.strings.AzStringConstants.ERROR_NONEXISTENT;
import static com.io7m.azeno.strings.AzStringConstants.ERROR_OPERATION_NOT_PERMITTED;
import static com.io7m.azeno.strings.AzStringConstants.USER_ID;

/**
 * @see AzACommandRolesRevoke
 */

public final class AzACmdRolesRevoke extends AzACmdAbstract<AzACommandRolesRevoke>
{
  /**
   * @see AzACommandRolesRevoke
   */

  public AzACmdRolesRevoke()
  {

  }

  @Override
  protected AzAResponseType executeActual(
    final AzACommandContext context,
    final AzACommandRolesRevoke command)
    throws DDatabaseException, AzCommandExecutionFailure
  {
    final var subject =
      context.session()
        .subject();

    /*
     * Does the current subject have all the roles that are being revoked,
     * or is the current subject an administrator?
     */

    final var rolesTaken =
      command.roles();
    final var rolesHeld =
      subject.roles();

    if (rolesHeld.contains(ROLE_ASSET_ADMIN)) {
      return revokeRoles(context, command, rolesTaken);
    }

    if (rolesHeld.containsAll(rolesTaken)) {
      return revokeRoles(context, command, rolesTaken);
    }

    throw context.failFormatted(
      400,
      errorOperationNotPermitted(),
      Map.of(USER_ID, command.user().toString()),
      ERROR_OPERATION_NOT_PERMITTED
    );
  }

  private static AzAResponseOK revokeRoles(
    final AzACommandContext context,
    final AzACommandRolesRevoke command,
    final Set<MRoleName> rolesTaken)
    throws DDatabaseException, AzCommandExecutionFailure
  {
    final var transaction =
      context.transaction();
    final var get =
      transaction.query(AzUserGetType.class);
    final var put =
      transaction.query(AzUserPutType.class);

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

    final var newRoles = new HashSet<>(targetUser.subject().roles());
    newRoles.removeAll(rolesTaken);
    put.execute(
      new AzUser(
        targetUser.userId(),
        targetUser.name(),
        new MSubject(newRoles)
      )
    );
    return new AzAResponseOK(context.requestId());
  }
}
