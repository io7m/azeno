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

import com.io7m.azeno.server.api.AzServerFactoryType;
import com.io7m.azeno.server.basic.AzServers;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceFactoryType;

/**
 * Identity server (Server basic implementation)
 */

module com.io7m.azeno.server.basic
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  uses AzServerTelemetryServiceFactoryType;

  requires com.io7m.azeno.database.api;
  requires com.io7m.azeno.error_codes;
  requires com.io7m.azeno.protocol.asset.cb;
  requires com.io7m.azeno.protocol.asset;
  requires com.io7m.azeno.security;
  requires com.io7m.azeno.server.api;
  requires com.io7m.azeno.server.asset.v1;
  requires com.io7m.azeno.server.service.clock;
  requires com.io7m.azeno.server.service.configuration;
  requires com.io7m.azeno.server.service.health;
  requires com.io7m.azeno.server.service.idstore;
  requires com.io7m.azeno.server.service.maintenance;
  requires com.io7m.azeno.server.service.reqlimit;
  requires com.io7m.azeno.server.service.sessions;
  requires com.io7m.azeno.server.service.telemetry.api;
  requires com.io7m.azeno.server.service.tls;
  requires com.io7m.azeno.server.service.verdant;

  requires com.io7m.azeno.strings;
  requires com.io7m.darco.api;
  requires com.io7m.jmulticlose.core;
  requires com.io7m.repetoir.core;
  requires io.helidon.webserver;
  requires io.opentelemetry.api;
  requires com.io7m.azeno.model;
  requires com.io7m.idstore.model;
  requires com.io7m.medrina.api;

  provides AzServerFactoryType
    with AzServers;

  exports com.io7m.azeno.server.basic;
}
