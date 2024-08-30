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

import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.azeno.security.AzSecurity;
import com.io7m.azeno.security.AzSecurityException;
import com.io7m.azeno.server.controller.command_exec.AzCommandContext;
import com.io7m.azeno.server.service.sessions.AzSession;
import com.io7m.medrina.api.MActionName;
import com.io7m.medrina.api.MObject;
import com.io7m.repetoir.core.RPServiceDirectoryType;

import java.util.UUID;

/**
 * The command context for asset API commands.
 */

public final class AzACommandContext
  extends AzCommandContext<AzAResponseType>
{
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

  public AzACommandContext(
    final RPServiceDirectoryType inServices,
    final UUID inRequestId,
    final AzDatabaseTransactionType inTransaction,
    final AzSession inSession,
    final String inRemoteHost,
    final String inRemoteUserAgent)
  {
    super(
      inServices,
      inRequestId,
      inTransaction,
      inSession,
      inRemoteHost,
      inRemoteUserAgent
    );
  }

  /**
   * Perform a security check for the given action.
   *
   * @param object The object
   * @param action The action
   *
   * @throws AzSecurityException On security policy denials
   */

  public void securityCheck(
    final MObject object,
    final MActionName action)
    throws AzSecurityException
  {
    AzSecurity.check(
      this.session().userId().id(),
      this.session().subject(),
      object,
      action
    );
  }
}
