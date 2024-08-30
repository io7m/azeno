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

import com.io7m.azeno.protocol.api.AzProtocolMessageValidatorType;
import com.io7m.azeno.protocol.asset.AzACommandLogin;
import com.io7m.cedarbridge.runtime.api.CBString;
import com.io7m.cedarbridge.runtime.convenience.CBMaps;
import com.io7m.idstore.model.IdName;

/**
 * A validator.
 */

public enum AzA1VCommandLogin
  implements AzProtocolMessageValidatorType<AzACommandLogin, AzA1CommandLogin>
{
  /**
   * A validator.
   */

  COMMAND_LOGIN;

  @Override
  public AzA1CommandLogin convertToWire(
    final AzACommandLogin c)
  {
    return new AzA1CommandLogin(
      new CBString(c.userName().value()),
      new CBString(c.password()),
      CBMaps.ofMapString(c.metadata())
    );
  }

  @Override
  public AzACommandLogin convertFromWire(
    final AzA1CommandLogin m)
  {
    return new AzACommandLogin(
      new IdName(m.fieldUserName().value()),
      m.fieldPassword().value(),
      CBMaps.toMapString(m.fieldMetadata())
    );
  }
}
