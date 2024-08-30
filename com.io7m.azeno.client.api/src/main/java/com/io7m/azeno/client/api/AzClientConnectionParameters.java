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

package com.io7m.azeno.client.api;

import com.io7m.hibiscus.api.HBConnectionParametersType;
import com.io7m.idstore.model.IdName;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * The client credentials.
 *
 * @param host           The target host
 * @param port           The target port
 * @param https          {@code true} if https should be used
 * @param username       The username
 * @param password       The password
 * @param metadata       The extra metadata
 * @param commandTimeout The command timeout
 * @param loginTimeout   The login timeout
 */

public record AzClientConnectionParameters(
  String host,
  int port,
  boolean https,
  IdName username,
  String password,
  Map<String, String> metadata,
  Duration loginTimeout,
  Duration commandTimeout)
  implements HBConnectionParametersType
{
  /**
   * The client credentials.
   *
   * @param host           The target host
   * @param port           The target port
   * @param https          {@code true} if https should be used
   * @param username       The username
   * @param password       The password
   * @param metadata       The extra metadata
   * @param commandTimeout The command timeout
   * @param loginTimeout   The login timeout
   */

  public AzClientConnectionParameters
  {
    Objects.requireNonNull(host, "host");
    Objects.requireNonNull(username, "username");
    Objects.requireNonNull(password, "password");
    Objects.requireNonNull(metadata, "metadata");
    Objects.requireNonNull(loginTimeout, "connectTimeout");
    Objects.requireNonNull(commandTimeout, "commandTimeout");
  }

  /**
   * @return The server base URI
   */

  public URI baseURI()
  {
    final var builder = new StringBuilder(128);
    if (this.https) {
      builder.append("https://");
    } else {
      builder.append("http://");
    }
    builder.append(this.host);
    builder.append(':');
    builder.append(this.port);
    builder.append('/');
    return URI.create(builder.toString());
  }
}
