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

package com.io7m.azeno.server.api;

import com.io7m.azeno.tls.AzTLSConfigurationType;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration for individual HTTP services.
 *
 * @param listenAddress     The listen address
 * @param listenPort        The listen port
 * @param externalAddress   The externally visible address
 * @param sessionExpiration The session expiration duration, if sessions should
 *                          expire
 * @param tlsConfiguration  The TLS configuration
 */

public record AzServerHTTPServiceConfiguration(
  String listenAddress,
  int listenPort,
  URI externalAddress,
  Optional<Duration> sessionExpiration,
  AzTLSConfigurationType tlsConfiguration)
{
  /**
   * Configuration for the part of the server that serves over HTTP.
   *
   * @param listenAddress     The listen address
   * @param listenPort        The listen port
   * @param externalAddress   The externally visible address
   * @param sessionExpiration The session expiration duration, if sessions
   *                          should expire
   * @param tlsConfiguration  The TLS configuration
   */

  public AzServerHTTPServiceConfiguration
  {
    Objects.requireNonNull(listenAddress, "listenAddress");
    Objects.requireNonNull(externalAddress, "externalAddress");
    Objects.requireNonNull(sessionExpiration, "sessionExpiration");
    Objects.requireNonNull(tlsConfiguration, "tlsConfiguration");
  }
}
