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


package com.io7m.azeno.protocol.asset.cb;

import com.io7m.azeno.protocol.api.AzProtocolException;
import com.io7m.azeno.protocol.api.AzProtocolMessageValidatorType;
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
import com.io7m.azeno.protocol.asset.AzAMessageType;
import com.io7m.azeno.protocol.asset.AzAResponseAuditSearch;
import com.io7m.azeno.protocol.asset.AzAResponseError;
import com.io7m.azeno.protocol.asset.AzAResponseLogin;
import com.io7m.azeno.protocol.asset.AzAResponseOK;
import com.io7m.azeno.protocol.asset.AzAResponseRolesGet;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.azeno.protocol.asset.AzATransactionResponse;

import static com.io7m.azeno.protocol.asset.cb.AzA1VCommandAuditSearchBegin.COMMAND_AUDIT_SEARCH_BEGIN;
import static com.io7m.azeno.protocol.asset.cb.AzA1VCommandAuditSearchNext.COMMAND_AUDIT_SEARCH_NEXT;
import static com.io7m.azeno.protocol.asset.cb.AzA1VCommandAuditSearchPrevious.COMMAND_AUDIT_SEARCH_PREVIOUS;
import static com.io7m.azeno.protocol.asset.cb.AzA1VCommandLogin.COMMAND_LOGIN;
import static com.io7m.azeno.protocol.asset.cb.AzA1VCommandRolesAssign.COMMAND_ROLES_ASSIGN;
import static com.io7m.azeno.protocol.asset.cb.AzA1VCommandRolesGet.COMMAND_ROLES_GET;
import static com.io7m.azeno.protocol.asset.cb.AzA1VCommandRolesRevoke.COMMAND_ROLES_REVOKE;
import static com.io7m.azeno.protocol.asset.cb.AzA1VResponseAuditSearch.RESPONSE_AUDIT_SEARCH;
import static com.io7m.azeno.protocol.asset.cb.AzA1VResponseError.RESPONSE_ERROR;
import static com.io7m.azeno.protocol.asset.cb.AzA1VResponseLogin.RESPONSE_LOGIN;
import static com.io7m.azeno.protocol.asset.cb.AzA1VResponseOK.RESPONSE_OK;
import static com.io7m.azeno.protocol.asset.cb.AzA1VResponseRolesGet.RESPONSE_ROLES_GET;

/**
 * A validator.
 */

public enum AzA1VMessage
  implements AzProtocolMessageValidatorType<AzAMessageType, ProtocolAzAv1Type>
{
  /**
   * A validator.
   */

  MESSAGE;

  @Override
  public ProtocolAzAv1Type convertToWire(
    final AzAMessageType message)
    throws AzProtocolException
  {
    return switch (message) {
      case final AzACommandType<?> m -> {
        yield convertToWireCommand(m);
      }
      case final AzAResponseType m -> {
        yield convertToWireResponse(m);
      }
      case final AzATransactionResponse m -> {
        yield convertToWireTransactionResponse(m);
      }
    };
  }

  private static ProtocolAzAv1Type convertToWireTransactionResponse(
    final AzATransactionResponse m)
  {
    throw new IllegalStateException("Unimplemented code");
  }

  private static ProtocolAzAv1Type convertToWireResponse(
    final AzAResponseType m)
  {
    return switch (m) {
      case final AzAResponseAuditSearch r -> {
        yield RESPONSE_AUDIT_SEARCH.convertToWire(r);
      }
      case final AzAResponseError r -> {
        yield RESPONSE_ERROR.convertToWire(r);
      }
      case final AzAResponseLogin r -> {
        yield RESPONSE_LOGIN.convertToWire(r);
      }
      case final AzAResponseOK r -> {
        yield RESPONSE_OK.convertToWire(r);
      }
      case final AzAResponseRolesGet r -> {
        yield RESPONSE_ROLES_GET.convertToWire(r);
      }
    };
  }

  private static ProtocolAzAv1Type convertToWireCommand(
    final AzACommandType<?> m)
    throws AzProtocolException
  {
    return switch (m) {
      case final AzACommandAuditSearchBegin c -> {
        yield COMMAND_AUDIT_SEARCH_BEGIN.convertToWire(c);
      }
      case final AzACommandAuditSearchNext c -> {
        yield COMMAND_AUDIT_SEARCH_NEXT.convertToWire(c);
      }
      case final AzACommandAuditSearchPrevious c -> {
        yield COMMAND_AUDIT_SEARCH_PREVIOUS.convertToWire(c);
      }
      case final AzACommandDebugInvalid c -> {
        throw new IllegalStateException(
          "Cannot serialize messages of type " + c.getClass()
        );
      }
      case final AzACommandDebugRandom c -> {
        throw new IllegalStateException(
          "Cannot serialize messages of type " + c.getClass()
        );
      }
      case final AzACommandLogin c -> {
        yield COMMAND_LOGIN.convertToWire(c);
      }
      case final AzACommandRolesAssign c -> {
        yield COMMAND_ROLES_ASSIGN.convertToWire(c);
      }
      case final AzACommandRolesGet c -> {
        yield COMMAND_ROLES_GET.convertToWire(c);
      }
      case final AzACommandRolesRevoke c -> {
        yield COMMAND_ROLES_REVOKE.convertToWire(c);
      }
    };
  }

  @Override
  public AzAMessageType convertFromWire(
    final ProtocolAzAv1Type message)
    throws AzProtocolException
  {
    return switch (message) {
      case final AzA1CommandLogin m -> {
        yield COMMAND_LOGIN.convertFromWire(m);
      }
      case final AzA1ResponseAuditSearch m -> {
        yield RESPONSE_AUDIT_SEARCH.convertFromWire(m);
      }
      case final AzA1ResponseLogin m -> {
        yield RESPONSE_LOGIN.convertFromWire(m);
      }
      case final AzA1ResponseRolesGet m -> {
        yield RESPONSE_ROLES_GET.convertFromWire(m);
      }
      case final AzA1CommandAuditSearchBegin m -> {
        yield COMMAND_AUDIT_SEARCH_BEGIN.convertFromWire(m);
      }
      case final AzA1CommandAuditSearchNext m -> {
        yield COMMAND_AUDIT_SEARCH_NEXT.convertFromWire(m);
      }
      case final AzA1CommandAuditSearchPrevious m -> {
        yield COMMAND_AUDIT_SEARCH_PREVIOUS.convertFromWire(m);
      }
      case final AzA1CommandRolesAssign m -> {
        yield COMMAND_ROLES_ASSIGN.convertFromWire(m);
      }
      case final AzA1CommandRolesGet m -> {
        yield COMMAND_ROLES_GET.convertFromWire(m);
      }
      case final AzA1CommandRolesRevoke m -> {
        yield COMMAND_ROLES_REVOKE.convertFromWire(m);
      }
      case final AzA1ResponseError m -> {
        yield RESPONSE_ERROR.convertFromWire(m);
      }
      case final AzA1ResponseOK m -> {
        yield RESPONSE_OK.convertFromWire(m);
      }
    };
  }
}
