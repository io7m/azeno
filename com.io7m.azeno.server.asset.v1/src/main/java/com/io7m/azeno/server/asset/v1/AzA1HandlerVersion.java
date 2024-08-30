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

import com.io7m.azeno.model.AzVersion;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctional;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctionalCoreType;
import com.io7m.azeno.server.http.AzHTTPResponseFixedSize;
import com.io7m.azeno.server.http.AzHTTPResponseType;
import com.io7m.repetoir.core.RPServiceDirectoryType;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static com.io7m.azeno.server.http.AzHTTPHandlerCoreInstrumented.withInstrumentation;

/**
 * A servlet for showing the server version.
 */

public final class AzA1HandlerVersion
  extends AzHTTPHandlerFunctional
{
  /**
   * A servlet for showing the server version.
   *
   * @param services The services
   */

  public AzA1HandlerVersion(
    final RPServiceDirectoryType services)
  {
    super(createCore(services));
  }

  private static AzHTTPHandlerFunctionalCoreType createCore(
    final RPServiceDirectoryType services)
  {
    return (request, information) -> {
      return withInstrumentation(
        services,
        (req0, info0) -> {
          return execute();
        }
      ).execute(request, information);
    };
  }

  private static AzHTTPResponseType execute()
  {
    final var text =
      String.format(
        "com.io7m.azeno %s %s\r\n\r\n",
        AzVersion.MAIN_VERSION,
        AzVersion.MAIN_BUILD
      );

    return new AzHTTPResponseFixedSize(
      200,
      Set.of(),
      "text/plain",
      text.getBytes(StandardCharsets.UTF_8)
    );
  }
}
