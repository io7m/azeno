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
import com.io7m.azeno.strings.AzStrings;
import com.io7m.genevan.core.GenProtocolClientHandlerType;

import java.net.URI;
import java.net.http.HttpClient;

/**
 * The type of protocol transport factories.
 */

public interface AzTransportFactoryType
  extends GenProtocolClientHandlerType
{
  /**
   * Create a new transport.
   *
   * @param configuration The configuration
   * @param inHttpClient  The underlying HTTP client
   * @param inStrings     The string resources
   * @param baseURI       The base URI negotiated by the server
   *
   * @return A new handler
   */

  AzTransportType createTransport(
    AzClientConfiguration configuration,
    HttpClient inHttpClient,
    AzStrings inStrings,
    URI baseURI
  );
}
