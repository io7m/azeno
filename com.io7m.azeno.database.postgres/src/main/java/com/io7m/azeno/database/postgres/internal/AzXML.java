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


package com.io7m.azeno.database.postgres.internal;

import com.io7m.azeno.xml.AzAssetParsers;
import com.io7m.azeno.xml.AzAssetSerializers;
import com.io7m.azeno.xml.AzSchemaParsers;
import com.io7m.azeno.xml.AzSchemaSerializers;

final class AzXML
{
  private static final AzSchemaParsers SCHEMA_PARSERS =
    new AzSchemaParsers();
  private static final AzSchemaSerializers SCHEMA_SERIALIZERS =
    new AzSchemaSerializers();
  private static final AzAssetParsers ASSET_PARSERS =
    new AzAssetParsers();
  private static final AzAssetSerializers ASSET_SERIALIZERS =
    new AzAssetSerializers();

  private AzXML()
  {

  }

  static AzSchemaParsers schemaParsers()
  {
    return SCHEMA_PARSERS;
  }

  static AzSchemaSerializers schemaSerializers()
  {
    return SCHEMA_SERIALIZERS;
  }

  static AzAssetParsers assetParsers()
  {
    return ASSET_PARSERS;
  }

  static AzAssetSerializers assetSerializers()
  {
    return ASSET_SERIALIZERS;
  }
}
