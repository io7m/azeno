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
import com.io7m.azeno.client.api.AzClientException;
import com.io7m.azeno.error_codes.AzStandardErrorCodes;
import com.io7m.azeno.strings.AzStringConstants;
import com.io7m.azeno.strings.AzStrings;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * The abstract base class for handlers.
 */

abstract class AzHandlerAbstract
  implements AzHandlerType
{
  private final AzStrings strings;
  private final AzClientConfiguration configuration;

  protected AzHandlerAbstract(
    final AzClientConfiguration inConfiguration,
    final AzStrings inStrings)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
  }

  @Override
  public final Function<Throwable, AzClientException> exceptionTransformer()
  {
    return AzClientException::ofException;
  }

  protected final AzStrings strings()
  {
    return this.strings;
  }

  protected final AzClientConfiguration configuration()
  {
    return this.configuration;
  }

  protected final AzClientException onNotConnected()
  {
    return new AzClientException(
      this.strings.format(AzStringConstants.ERROR_NOT_LOGGED_IN),
      AzStandardErrorCodes.errorNotLoggedIn(),
      Map.of(),
      Optional.empty(),
      Optional.empty()
    );
  }
}
