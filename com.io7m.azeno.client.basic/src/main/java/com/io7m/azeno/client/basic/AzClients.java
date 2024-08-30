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

package com.io7m.azeno.client.basic;

import com.io7m.azeno.client.api.AzClientConfiguration;
import com.io7m.azeno.client.api.AzClientException;
import com.io7m.azeno.client.api.AzClientFactoryType;
import com.io7m.azeno.client.api.AzClientType;
import com.io7m.azeno.client.basic.internal.AzClient;
import com.io7m.azeno.error_codes.AzStandardErrorCodes;
import com.io7m.azeno.strings.AzStrings;

import java.net.CookieManager;
import java.net.http.HttpClient;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * The default client factory.
 */

public final class AzClients
  implements AzClientFactoryType
{
  /**
   * The default client factory.
   */

  public AzClients()
  {

  }

  @Override
  public String toString()
  {
    return String.format(
      "[AzClients 0x%08x]",
      Integer.valueOf(this.hashCode())
    );
  }

  @Override
  public String description()
  {
    return "Digital asset client service.";
  }

  private static AzStrings openStrings(
    final Locale locale)
    throws AzClientException
  {
    try {
      return AzStrings.create(locale);
    } catch (final Exception e) {
      throw new AzClientException(
        e.getMessage(),
        e,
        AzStandardErrorCodes.errorIo(),
        Map.of(),
        Optional.empty(),
        Optional.empty()
      );
    }
  }

  @Override
  public AzClientType create(
    final AzClientConfiguration configuration)
    throws AzClientException
  {
    final var locale =
      configuration.locale();
    final var strings =
      openStrings(locale);

    final Supplier<HttpClient> clients = () -> {
      return HttpClient.newBuilder()
        .cookieHandler(new CookieManager())
        .executor(Executors.newVirtualThreadPerTaskExecutor())
        .build();
    };

    return new AzClient(configuration, strings, clients);
  }
}
