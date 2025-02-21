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

package com.io7m.azeno.server.asset.v1;

import com.io7m.azeno.server.http.AzHTTPHandlerFunctional;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctionalCoreType;
import com.io7m.azeno.server.http.AzHTTPResponseFixedSize;
import com.io7m.azeno.server.http.AzHTTPResponseType;
import com.io7m.azeno.server.service.health.AzServerHealth;
import com.io7m.repetoir.core.RPServiceDirectoryType;

import java.util.Objects;
import java.util.Set;

import static com.io7m.azeno.server.http.AzHTTPHandlerCoreInstrumented.withInstrumentation;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The schema_v1 health servlet.
 */

public final class AzA1HandlerHealth
  extends AzHTTPHandlerFunctional
{
  /**
   * The schema_v1 health servlet.
   *
   * @param services The services
   */

  public AzA1HandlerHealth(
    final RPServiceDirectoryType services)
  {
    super(createCore(services));
  }

  private static AzHTTPHandlerFunctionalCoreType createCore(
    final RPServiceDirectoryType services)
  {
    final var health =
      services.requireService(AzServerHealth.class);

    final AzHTTPHandlerFunctionalCoreType main =
      (request, information) -> execute(health);

    return withInstrumentation(services, main);
  }

  private static AzHTTPResponseType execute(
    final AzServerHealth health)
  {
    final var status =
      health.status();
    final var statusCode =
      Objects.equals(status, AzServerHealth.statusOKText()) ? 200 : 500;

    return new AzHTTPResponseFixedSize(
      statusCode,
      Set.of(),
      "text/plain",
      status.getBytes(UTF_8)
    );
  }
}
