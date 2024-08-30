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


package com.io7m.azeno.server.service.tls;


import com.io7m.azeno.error_codes.AzException;
import com.io7m.azeno.error_codes.AzStandardErrorCodes;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType;
import com.io7m.azeno.strings.AzStringConstants;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.azeno.tls.AzTLSContext;
import com.io7m.azeno.tls.AzTLSStoreConfiguration;
import com.io7m.repetoir.core.RPServiceDirectoryType;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType.recordSpanException;

/**
 * The TLS context service.
 */

public final class AzTLSContextService
  implements AzTLSContextServiceType
{
  private final ConcurrentHashMap.KeySetView<AzTLSContext, Boolean> contexts;
  private final AzServerTelemetryServiceType telemetry;
  private final AzStrings strings;

  private AzTLSContextService(
    final AzServerTelemetryServiceType inTelemetry,
    final AzStrings inStrings)
  {
    this.telemetry =
      Objects.requireNonNull(inTelemetry, "telemetry");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.contexts =
      ConcurrentHashMap.newKeySet();
  }

  @Override
  public String toString()
  {
    return "[AzTLSContextService 0x%x]"
      .formatted(Integer.valueOf(this.hashCode()));
  }

  /**
   * @param services The service directory
   *
   * @return A new TLS context service
   */

  public static AzTLSContextServiceType createService(
    final RPServiceDirectoryType services)
  {
    return new AzTLSContextService(
      services.requireService(AzServerTelemetryServiceType.class),
      services.requireService(AzStrings.class)
    );
  }

  @Override
  public AzTLSContext create(
    final String user,
    final AzTLSStoreConfiguration keyStoreConfiguration,
    final AzTLSStoreConfiguration trustStoreConfiguration)
    throws AzException
  {
    try {
      final var newContext =
        AzTLSContext.create(
          user,
          keyStoreConfiguration,
          trustStoreConfiguration
        );
      this.contexts.add(newContext);
      return newContext;
    } catch (final IOException e) {
      throw errorIO(this.strings, e);
    } catch (final GeneralSecurityException e) {
      throw errorSecurity(e);
    }
  }

  @Override
  public void reload()
  {
    final var span =
      this.telemetry.tracer()
        .spanBuilder("ReloadTLSContexts")
        .startSpan();

    try (var ignored = span.makeCurrent()) {
      for (final var context : this.contexts) {
        this.reloadContext(context);
      }
    } finally {
      span.end();
    }
  }

  private void reloadContext(
    final AzTLSContext context)
  {
    final var span =
      this.telemetry.tracer()
        .spanBuilder("ReloadTLSContext")
        .startSpan();

    try (var ignored = span.makeCurrent()) {
      context.reload();
    } catch (final Throwable e) {
      recordSpanException(e);
    } finally {
      span.end();
    }
  }

  @Override
  public String description()
  {
    return "The TLS context service.";
  }

  private static AzException errorIO(
    final AzStrings strings,
    final IOException e)
  {
    return new AzException(
      strings.format(AzStringConstants.ERROR_IO),
      e,
      AzStandardErrorCodes.errorIo(),
      Map.of(),
      Optional.empty()
    );
  }

  private static AzException errorSecurity(
    final GeneralSecurityException e)
  {
    return new AzException(
      e.getMessage(),
      e,
      AzStandardErrorCodes.errorIo(),
      Map.of(),
      Optional.empty()
    );
  }
}
