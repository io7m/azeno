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

import com.io7m.azeno.model.AzAuditEvent;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.protocol.api.AzProtocolMessageValidatorType;
import com.io7m.cedarbridge.runtime.api.CBUUID;
import com.io7m.cedarbridge.runtime.convenience.CBMaps;
import com.io7m.cedarbridge.runtime.time.CBOffsetDateTime;

import static com.io7m.cedarbridge.runtime.api.CBCore.string;
import static com.io7m.cedarbridge.runtime.api.CBCore.unsigned64;

/**
 * A validator.
 */

public enum AzA1VAuditEvent
  implements AzProtocolMessageValidatorType<AzAuditEvent, AzA1AuditEvent>
{
  /**
   * A validator.
   */

  AUDIT_EVENT;

  @Override
  public AzA1AuditEvent convertToWire(
    final AzAuditEvent message)
  {
    return new AzA1AuditEvent(
      unsigned64(message.id()),
      new CBOffsetDateTime(message.time()),
      new CBUUID(message.owner().id()),
      string(message.type()),
      CBMaps.ofMapString(message.data())
    );
  }

  @Override
  public AzAuditEvent convertFromWire(
    final AzA1AuditEvent message)
  {
    return new AzAuditEvent(
      message.fieldId().value(),
      message.fieldTime().value(),
      new AzUserID(message.fieldOwner().value()),
      message.fieldType().value(),
      CBMaps.toMapString(message.fieldData())
    );
  }
}
