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


package com.io7m.azeno.server.service.configuration.v1;

import com.io7m.azeno.server.api.AzServerHTTPServiceConfiguration;
import com.io7m.azeno.tls.AzTLSConfigurationType;
import com.io7m.azeno.tls.AzTLSDisabled;
import com.io7m.azeno.tls.AzTLSEnabled;
import com.io7m.blackthorne.core.BTElementHandlerConstructorType;
import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;
import com.io7m.blackthorne.core.BTQualifiedName;
import org.xml.sax.Attributes;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static com.io7m.azeno.server.service.configuration.v1.AzC1Names.tlsQName;
import static java.util.Map.entry;

final class AzC1AssetService
  implements BTElementHandlerType<Object, AzServerHTTPServiceConfiguration>
{
  private String listenAddress;
  private int listenPort;
  private URI externalAddress;
  private AzTLSConfigurationType tls;
  private Optional<Duration> sessionExpiration;

  AzC1AssetService(
    final BTElementParsingContextType context)
  {
    this.sessionExpiration =
      Optional.empty();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      entry(tlsQName("TLSEnabled"), AzC1TLSEnabled::new),
      entry(tlsQName("TLSDisabled"), AzC1TLSDisabled::new)
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
    throws Exception
  {
    switch (result) {
      case final AzTLSEnabled s -> {
        this.tls = s;
      }
      case final AzTLSDisabled s -> {
        this.tls = s;
      }
      default -> {
        throw new IllegalArgumentException(
          "Unrecognized element: %s".formatted(result)
        );
      }
    }
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.listenAddress =
      attributes.getValue("ListenAddress");
    this.listenPort =
      Integer.parseUnsignedInt(attributes.getValue("ListenPort"));
    this.externalAddress =
      URI.create(attributes.getValue("ExternalAddress"));
    this.sessionExpiration =
      Optional.ofNullable(attributes.getValue("SessionExpiration"))
        .map(AzC1Durations::parse);
  }

  @Override
  public AzServerHTTPServiceConfiguration onElementFinished(
    final BTElementParsingContextType context)
    throws Exception
  {
    return new AzServerHTTPServiceConfiguration(
      this.listenAddress,
      this.listenPort,
      this.externalAddress,
      this.sessionExpiration,
      this.tls
    );
  }
}
