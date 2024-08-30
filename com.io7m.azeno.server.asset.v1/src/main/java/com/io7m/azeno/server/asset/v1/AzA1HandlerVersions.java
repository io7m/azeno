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

import com.io7m.azeno.protocol.asset.cb.AzA1Messages;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctional;
import com.io7m.azeno.server.http.AzHTTPHandlerFunctionalCoreType;
import com.io7m.azeno.server.http.AzHTTPResponseFixedSize;
import com.io7m.azeno.server.http.AzHTTPResponseType;
import com.io7m.azeno.server.service.verdant.AzVerdantMessagesType;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import com.io7m.verdant.core.VProtocolException;
import com.io7m.verdant.core.VProtocolSupported;
import com.io7m.verdant.core.VProtocols;

import java.util.List;
import java.util.Set;

import static com.io7m.azeno.server.http.AzHTTPHandlerCoreInstrumented.withInstrumentation;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The schema_v1 version servlet.
 */

public final class AzA1HandlerVersions
  extends AzHTTPHandlerFunctional
{
  private static final VProtocols PROTOCOLS =
    createProtocols();

  /**
   * The schema_v1 version servlet.
   *
   * @param services The services
   */

  public AzA1HandlerVersions(
    final RPServiceDirectoryType services)
  {
    super(createCore(services));
  }

  private static AzHTTPHandlerFunctionalCoreType createCore(
    final RPServiceDirectoryType services)
  {
    final var messages =
      services.requireService(AzVerdantMessagesType.class);

    return (request, information) -> {
      return withInstrumentation(
        services,
        (req0, info0) -> {
          return execute(messages);
        }
      ).execute(request, information);
    };
  }

  private static AzHTTPResponseType execute(
    final AzVerdantMessagesType messages)
  {
    try {
      return new AzHTTPResponseFixedSize(
        200,
        Set.of(),
        AzVerdantMessagesType.contentType(),
        messages.serialize(PROTOCOLS, 1)
      );
    } catch (final VProtocolException e) {
      return new AzHTTPResponseFixedSize(
        500,
        Set.of(),
        "text/plain",
        e.getMessage().getBytes(UTF_8)
      );
    }
  }

  private static VProtocols createProtocols()
  {
    return new VProtocols(List.of(
      new VProtocolSupported(
        AzA1Messages.protocolId(),
        1L,
        0L,
        "/asset/1/0/"
      )
    ));
  }
}
