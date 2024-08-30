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

package com.io7m.azeno.client.basic.internal;

import com.io7m.azeno.client.api.AzClientConfiguration;
import com.io7m.azeno.protocol.asset.cb.AzA1Messages;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.genevan.core.GenProtocolIdentifier;
import com.io7m.genevan.core.GenProtocolVersion;

import java.net.URI;
import java.net.http.HttpClient;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

/**
 * The factory of version 1 protocol transports.
 */

public final class AzTransports1
  implements AzTransportFactoryType
{
  /**
   * The factory of version 1 protocol transports.
   */

  public AzTransports1()
  {

  }

  @Override
  public GenProtocolIdentifier supported()
  {
    return new GenProtocolIdentifier(
      AzA1Messages.protocolId().toString(),
      new GenProtocolVersion(ONE, ZERO)
    );
  }

  @Override
  public AzTransportType createTransport(
    final AzClientConfiguration configuration,
    final HttpClient inHttpClient,
    final AzStrings inStrings,
    final URI inBaseURI)
  {
    return new AzTransport1(
      configuration.clock(),
      inStrings,
      inHttpClient,
      inBaseURI
    );
  }

  @Override
  public String toString()
  {
    return "[%s 0x%s]".formatted(
      this.getClass().getSimpleName(),
      Integer.toUnsignedString(this.hashCode(), 16)
    );
  }
}
