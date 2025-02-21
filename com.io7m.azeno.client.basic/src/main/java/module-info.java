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
 * Digital asset system (Client basic implementation)
 */

module com.io7m.azeno.client.basic
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires com.io7m.azeno.client.api;
  requires com.io7m.azeno.protocol.asset.cb;
  requires com.io7m.azeno.strings;
  requires com.io7m.azeno.error_codes;

  requires com.io7m.genevan.core;
  requires com.io7m.hibiscus.api;
  requires com.io7m.idstore.model;
  requires com.io7m.jxtrand.api;
  requires com.io7m.repetoir.core;
  requires com.io7m.verdant.core.cb;
  requires commons.math3;
  requires java.net.http;
  requires org.slf4j;
  requires com.io7m.azeno.protocol.asset;
  requires com.io7m.azeno.model;
  requires com.io7m.azeno.protocol.api;

  exports com.io7m.azeno.client.basic;
}
