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
import com.io7m.azeno.protocol.asset.AzAMessageType;

import static com.io7m.azeno.protocol.asset.cb.AzA1VMessage.MESSAGE;

/**
 * Functions to translate between the core command set and the digital asset schema_v1
 * Cedarbridge encoding command set.
 */

public final class AzA1Validation
  implements AzProtocolMessageValidatorType<AzAMessageType, ProtocolAzAv1Type>
{
  /**
   * Functions to translate between the core command set and the digital asset schema_v1
   * Cedarbridge encoding command set.
   */

  public AzA1Validation()
  {

  }

  @Override
  public ProtocolAzAv1Type convertToWire(
    final AzAMessageType message)
    throws AzProtocolException
  {
    try {
      return MESSAGE.convertToWire(message);
    } catch (final ProtocolUncheckedException e) {
      throw e.getCause();
    }
  }

  @Override
  public AzAMessageType convertFromWire(
    final ProtocolAzAv1Type message)
    throws AzProtocolException
  {
    try {
      return MESSAGE.convertFromWire(message);
    } catch (final ProtocolUncheckedException e) {
      throw e.getCause();
    }
  }
}
