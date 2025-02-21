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


package com.io7m.azeno.protocol.api;

/**
 * The interface exposed by protocol message handlers.
 *
 * @param <T> The type of protocol messages
 */

public interface AzProtocolMessagesType<T extends AzProtocolMessageType>
{
  /**
   * Parse a message from the given bytes.
   *
   * @param data The bytes
   *
   * @return A parsed message
   *
   * @throws AzProtocolException If parsing fails
   */

  T parse(byte[] data)
    throws AzProtocolException;

  /**
   * Serialize the given message to a byte array.
   *
   * @param message The message
   *
   * @return The serialized message as a byte array
   *
   * @throws AzProtocolException If serialization fails
   */

  byte[] serialize(T message)
    throws AzProtocolException;
}
