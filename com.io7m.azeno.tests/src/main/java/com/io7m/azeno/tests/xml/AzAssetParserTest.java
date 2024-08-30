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


package com.io7m.azeno.tests.xml;

import com.io7m.azeno.xml.AzAssetParsers;
import com.io7m.azeno.xml.AzAssetSerializers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class AzAssetParserTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AzAssetParserTest.class);

  private AzAssetParsers parsers;
  private AzAssetSerializers serializers;

  @BeforeEach
  public void setup()
  {
    this.parsers =
      new AzAssetParsers();
    this.serializers =
      new AzAssetSerializers();
  }

  @Test
  public void testExampleAsset()
    throws Exception
  {
    this.roundTrip("ExampleAsset.xml");
  }

  private void roundTrip(
    final String name)
    throws Exception
  {
    try (final var stream = AzAssetParserTest.class.getResourceAsStream(
      "/com/io7m/azeno/tests/%s".formatted(name)
    )) {
      final var schema0 =
        this.parsers.parse(
          URI.create("urn:first"),
          stream
        );
      final var streamOut =
        new ByteArrayOutputStream();

      this.serializers.serialize(
        URI.create("urn:first"),
        streamOut,
        schema0
      );

      LOG.debug("{}", new String(streamOut.toByteArray(), StandardCharsets.UTF_8));

      final var streamIn =
        new ByteArrayInputStream(streamOut.toByteArray());

      final var schema1 = this.parsers.parse(
        URI.create("urn:second"),
        streamIn
      );

      assertEquals(schema0, schema1);
    }
  }
}
