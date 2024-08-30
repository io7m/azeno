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

/**
 * The main distribution.
 */

open module com.io7m.azeno.main
{
  uses com.io7m.azeno.server.api.AzServerFactoryType;

  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;
  
  requires com.io7m.azeno.database.api;
  requires com.io7m.azeno.database.postgres;
  requires com.io7m.azeno.error_codes;
  requires com.io7m.azeno.model;
  requires com.io7m.azeno.protocol.api;
  requires com.io7m.azeno.protocol.asset.cb;
  requires com.io7m.azeno.protocol.asset;
  requires com.io7m.azeno.server.api;
  requires com.io7m.azeno.server.controller;
  requires com.io7m.azeno.server.http;
  requires com.io7m.azeno.server.asset.v1;
  requires com.io7m.azeno.server.service.clock;
  requires com.io7m.azeno.server.service.configuration;
  requires com.io7m.azeno.server.service.reqlimit;
  requires com.io7m.azeno.server.service.sessions;
  requires com.io7m.azeno.server.service.telemetry.api;
  requires com.io7m.azeno.server.service.telemetry.otp;
  requires com.io7m.azeno.server.service.verdant;

  requires com.io7m.canonmill.core;
  requires com.io7m.quarrel.core;
  requires com.io7m.quarrel.ext.logback;
  requires com.io7m.repetoir.core;
  requires jul.to.slf4j;
  requires org.slf4j;
  requires com.io7m.jade.api;
  requires com.io7m.jade.vanilla;
  requires com.io7m.anethum.api;
  requires com.io7m.anethum.slf4j;
  requires com.io7m.idstore.model;
  requires com.io7m.darco.api;

  exports com.io7m.azeno.main.internal
    to com.io7m.azeno.documentation, com.io7m.azeno.tests;

  exports com.io7m.azeno.main;
}
