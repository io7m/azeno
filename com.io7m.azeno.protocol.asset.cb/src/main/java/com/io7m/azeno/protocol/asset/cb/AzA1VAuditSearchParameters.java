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

import com.io7m.azeno.model.AzAuditSearchParameters;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.protocol.api.AzProtocolException;
import com.io7m.azeno.protocol.api.AzProtocolMessageValidatorType;
import com.io7m.cedarbridge.runtime.api.CBOptionType;
import com.io7m.cedarbridge.runtime.api.CBString;
import com.io7m.cedarbridge.runtime.api.CBUUID;

import static com.io7m.cedarbridge.runtime.api.CBCore.unsigned32;

/**
 * A validator.
 */

public enum AzA1VAuditSearchParameters
  implements AzProtocolMessageValidatorType<AzAuditSearchParameters, AzA1AuditSearchParameters>
{
  /**
   * A validator.
   */

  AUDIT_SEARCH_PARAMETERS;

  private static final AzA1VComparisonsExact<String, CBString> EXACT =
    new AzA1VComparisonsExact<>(AzA1VStrings.STRINGS);

  @Override
  public AzA1AuditSearchParameters convertToWire(
    final AzAuditSearchParameters parameters)
    throws AzProtocolException
  {
    return new AzA1AuditSearchParameters(
      CBOptionType.fromOptional(
        parameters.owner().map(AzUserID::id).map(CBUUID::new)),
      EXACT.convertToWire(parameters.type()),
      AzA1VTimeRange.TIME_RANGE.convertToWire(parameters.timeRange()),
      unsigned32(parameters.pageSize())
    );
  }

  @Override
  public AzAuditSearchParameters convertFromWire(
    final AzA1AuditSearchParameters message)
    throws AzProtocolException
  {
    return new AzAuditSearchParameters(
      message.fieldOwner().asOptional()
        .map(CBUUID::value)
        .map(AzUserID::new),
      EXACT.convertFromWire(message.fieldType()),
      AzA1VTimeRange.TIME_RANGE.convertFromWire(message.fieldTimeRange()),
      message.fieldPageSize().value()
    );
  }
}
