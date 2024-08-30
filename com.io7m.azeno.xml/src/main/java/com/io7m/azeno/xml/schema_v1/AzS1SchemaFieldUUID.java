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


package com.io7m.azeno.xml.schema_v1;

import com.io7m.azeno.model.AzSchemaFieldType;
import com.io7m.azeno.model.AzSchemaFieldUUID;
import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;
import com.io7m.lanark.core.RDottedName;
import org.xml.sax.Attributes;

import java.util.Objects;

/**
 * A parser.
 */

public final class AzS1SchemaFieldUUID
  implements BTElementHandlerType<Object, AzSchemaFieldType<?>>
{
  private RDottedName name;
  private boolean multiValued;

  /**
   * A parser.
   *
   * @param context The context
   */

  public AzS1SchemaFieldUUID(
    final BTElementParsingContextType context)
  {

  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.name =
      new RDottedName(attributes.getValue("Name"));
    this.multiValued =
      Boolean.parseBoolean(
        Objects.requireNonNullElse(attributes.getValue("MultiValued"), "false")
      );
  }

  @Override
  public AzSchemaFieldType<?> onElementFinished(
    final BTElementParsingContextType context)
  {
    return new AzSchemaFieldUUID(this.name, this.multiValued);
  }
}
