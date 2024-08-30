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
import com.io7m.azeno.protocol.asset.AzAResponseBlame;

/**
 * A validator.
 */

public enum AzA1VResponseBlame
  implements AzProtocolMessageValidatorType<AzAResponseBlame, AzA1ResponseBlame>
{
  /**
   * A validator.
   */

  RESPONSE_BLAME;

  @Override
  public AzA1ResponseBlame convertToWire(
    final AzAResponseBlame blame)
  {
    return switch (blame) {
      case BLAME_SERVER -> {
        yield new AzA1ResponseBlame.BlameServer();
      }
      case BLAME_CLIENT -> {
        yield new AzA1ResponseBlame.BlameClient();
      }
    };
  }

  @Override
  public AzAResponseBlame convertFromWire(
    final AzA1ResponseBlame blame)
  {
    return switch (blame) {
      case final AzA1ResponseBlame.BlameClient blameClient ->
        AzAResponseBlame.BLAME_CLIENT;
      case final AzA1ResponseBlame.BlameServer blameServer ->
        AzAResponseBlame.BLAME_SERVER;
    };
  }
}
