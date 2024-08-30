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
import com.io7m.azeno.protocol.asset.AzAResponseAuditSearch;
import com.io7m.cedarbridge.runtime.api.CBUUID;

import static com.io7m.azeno.protocol.asset.cb.AzA1VAuditEvent.AUDIT_EVENT;

/**
 * A validator.
 */

public enum AzA1VResponseAuditSearch
  implements AzProtocolMessageValidatorType<
    AzAResponseAuditSearch, AzA1ResponseAuditSearch>
{
  /**
   * A validator.
   */

  RESPONSE_AUDIT_SEARCH;

  @Override
  public AzA1ResponseAuditSearch convertToWire(
    final AzAResponseAuditSearch c)
  {
    return new AzA1ResponseAuditSearch(
      new CBUUID(c.requestId()),
      AzA1VPage.pageToWire(c.results(), AUDIT_EVENT::convertToWire)
    );
  }

  @Override
  public AzAResponseAuditSearch convertFromWire(
    final AzA1ResponseAuditSearch c)
  {
    return new AzAResponseAuditSearch(
      c.fieldRequestId().value(),
      AzA1VPage.pageFromWire(c.fieldResults(), AUDIT_EVENT::convertFromWire)
    );
  }
}
