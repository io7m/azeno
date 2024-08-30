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

import com.io7m.azeno.server.http.AzHTTPRequestTimeFilter;
import com.io7m.azeno.server.service.clock.AzServerClock;
import com.io7m.azeno.server.service.configuration.AzConfigurationServiceType;
import com.io7m.azeno.server.service.telemetry.api.AzMetricsServiceType;
import com.io7m.azeno.server.service.tls.AzTLSContextServiceType;
import com.io7m.azeno.tls.AzTLSEnabled;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import io.helidon.common.tls.TlsConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

import static java.net.StandardSocketOptions.SO_REUSEADDR;
import static java.net.StandardSocketOptions.SO_REUSEPORT;

/**
 * An asset API schema_v1 server.
 */

public final class AzA1Server
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AzA1Server.class);

  private AzA1Server()
  {

  }

  /**
   * Create an asset API schema_v1 server.
   *
   * @param services The service directory
   *
   * @return A server
   *
   * @throws Exception On errors
   */

  public static WebServer create(
    final RPServiceDirectoryType services)
    throws Exception
  {
    final var configurationService =
      services.requireService(AzConfigurationServiceType.class);
    final var tlsService =
      services.requireService(AzTLSContextServiceType.class);
    final var configuration =
      configurationService.configuration();
    final var httpConfig =
      configuration.assetApiConfiguration();
    final var address =
      InetSocketAddress.createUnresolved(
        httpConfig.listenAddress(),
        httpConfig.listenPort()
      );

    final var routing =
      HttpRouting.builder()
        .addFilter(new AzHTTPRequestTimeFilter(
          services.requireService(AzMetricsServiceType.class),
          services.requireService(AzServerClock.class)
        ))
        .get("/", new AzA1HandlerVersions(services))
        .post(
          "/asset/1/0/login",
          new AzA1HandlerLogin(services))
        .post(
          "/asset/1/0/command",
          new AzA1HandlerCommand(services))
        .post(
          "/asset/1/0/transaction",
          new AzA1HandlerTransaction(services))
        .get("/version", new AzA1HandlerVersion(services))
        .get("/health", new AzA1HandlerHealth(services));

    final var webServerBuilder =
      WebServerConfig.builder();

    if (httpConfig.tlsConfiguration() instanceof final AzTLSEnabled enabled) {
      final var tlsContext =
        tlsService.create(
          "AssetAPI",
          enabled.keyStore(),
          enabled.trustStore()
        );

      webServerBuilder.tls(
        TlsConfig.builder()
          .enabled(true)
          .sslContext(tlsContext.context())
          .build()
      );
    }

    final var webServer =
      webServerBuilder
        .port(httpConfig.listenPort())
        .address(InetAddress.getByName(httpConfig.listenAddress()))
        .listenerSocketOptions(Map.ofEntries(
          Map.entry(SO_REUSEADDR, Boolean.TRUE),
          Map.entry(SO_REUSEPORT, Boolean.TRUE)
        ))
        .routing(routing)
        .build();

    webServer.start();
    LOG.info("[{}] Digital asset API server started", address);
    return webServer;
  }
}
