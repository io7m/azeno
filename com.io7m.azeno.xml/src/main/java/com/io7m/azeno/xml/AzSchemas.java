/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.azeno.xml;

import com.io7m.jxe.core.JXESchemaDefinition;
import com.io7m.jxe.core.JXESchemaResolutionMappings;

import java.net.URI;

/**
 * Various XML schemas.
 */

public final class AzSchemas
{
  private static final JXESchemaDefinition SCHEMA_1 =
    JXESchemaDefinition.builder()
      .setFileIdentifier("schema-1.xsd")
      .setLocation(AzSchemas.class.getResource(
        "/com/io7m/azeno/xml/schema-1.xsd"))
      .setNamespace(URI.create("urn:com.io7m.azeno:schema:1"))
      .build();

  private static final JXESchemaDefinition ASSET_1 =
    JXESchemaDefinition.builder()
      .setFileIdentifier("asset-1.xsd")
      .setLocation(AzSchemas.class.getResource(
        "/com/io7m/azeno/xml/asset-1.xsd"))
      .setNamespace(URI.create("urn:com.io7m.azeno:asset:1"))
      .build();

  private static final JXESchemaResolutionMappings SCHEMA_MAPPINGS =
    JXESchemaResolutionMappings.builder()
      .putMappings(SCHEMA_1.namespace(), SCHEMA_1)
      .putMappings(ASSET_1.namespace(), ASSET_1)
      .build();

  /**
   * @return The Schema schema_v1 schema
   */

  public static JXESchemaDefinition schema1()
  {
    return SCHEMA_1;
  }

  /**
   * @return The Asset schema_v1 schema
   */

  public static JXESchemaDefinition asset1()
  {
    return ASSET_1;
  }

  /**
   * @return The set of supported schemas.
   */

  public static JXESchemaResolutionMappings schemas()
  {
    return SCHEMA_MAPPINGS;
  }

  private AzSchemas()
  {

  }
}
