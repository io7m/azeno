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

import com.io7m.azeno.error_codes.AzErrorCode;
import com.io7m.azeno.protocol.api.AzProtocolMessageValidatorType;
import com.io7m.azeno.protocol.asset.AzAResponseError;
import com.io7m.cedarbridge.runtime.api.CBOptionType;
import com.io7m.cedarbridge.runtime.api.CBString;
import com.io7m.cedarbridge.runtime.api.CBUUID;
import com.io7m.cedarbridge.runtime.convenience.CBLists;
import com.io7m.cedarbridge.runtime.convenience.CBMaps;

import java.util.Optional;

import static com.io7m.azeno.protocol.asset.cb.AzA1VError.ERROR;
import static com.io7m.azeno.protocol.asset.cb.AzA1VResponseBlame.RESPONSE_BLAME;

/**
 * A validator.
 */

public enum AzA1VResponseError
  implements AzProtocolMessageValidatorType<
    AzAResponseError, AzA1ResponseError>
{
  /**
   * A validator.
   */

  RESPONSE_ERROR;

  @Override
  public AzA1ResponseError convertToWire(
    final AzAResponseError c)
  {
    return new AzA1ResponseError(
      new CBUUID(c.requestId()),
      new CBString(c.errorCode().id()),
      new CBString(c.message()),
      CBMaps.ofMapString(c.attributes()),
      CBOptionType.fromOptional(c.remediatingAction().map(CBString::new)),
      RESPONSE_BLAME.convertToWire(c.blame()),
      CBLists.ofCollection(c.extras(), ERROR::convertToWire)
    );
  }

  @Override
  public AzAResponseError convertFromWire(
    final AzA1ResponseError m)
  {
    return new AzAResponseError(
      m.fieldRequestId().value(),
      m.fieldMessage().value(),
      new AzErrorCode(m.fieldErrorCode().value()),
      CBMaps.toMapString(m.fieldAttributes()),
      m.fieldRemediatingAction()
        .asOptional()
        .map(CBString::value),
      Optional.empty(),
      RESPONSE_BLAME.convertFromWire(m.fieldBlame()),
      CBLists.toList(m.fieldExtras(), ERROR::convertFromWire)
    );
  }
}
