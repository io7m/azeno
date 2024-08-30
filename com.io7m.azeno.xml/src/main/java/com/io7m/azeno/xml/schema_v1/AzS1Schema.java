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

import com.io7m.azeno.model.AzSchema;
import com.io7m.azeno.model.AzSchemaFieldType;
import com.io7m.azeno.model.AzSchemaID;
import com.io7m.blackthorne.core.BTElementHandlerConstructorType;
import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;
import com.io7m.blackthorne.core.BTQualifiedName;
import com.io7m.lanark.core.RDottedName;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;

/**
 * A parser.
 */

public final class AzS1Schema
  implements BTElementHandlerType<AzSchemaFieldType<?>, AzSchema>
{
  private final HashMap<RDottedName, AzSchemaFieldType<?>> fields;
  private AzSchemaID id;

  /**
   * The root schema parser.
   *
   * @param context The context
   */

  public AzS1Schema(
    final BTElementParsingContextType context)
  {
    this.fields = new HashMap<>();
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.id = new AzSchemaID(
      new RDottedName(attributes.getValue("Name")),
      Integer.parseUnsignedInt(attributes.getValue("Version"))
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final AzSchemaFieldType<?> result)
  {
    this.fields.put(result.name(), result);
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends AzSchemaFieldType<?>>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        AzS1Names.qName("FieldBoolean"),
        AzS1SchemaFieldBoolean::new
      ),
      Map.entry(
        AzS1Names.qName("FieldFloating"),
        AzS1SchemaFieldFloating::new
      ),
      Map.entry(
        AzS1Names.qName("FieldInteger"),
        AzS1SchemaFieldInteger::new
      ),
      Map.entry(
        AzS1Names.qName("FieldStringLocal"),
        AzS1SchemaFieldStringLocal::new
      ),
      Map.entry(
        AzS1Names.qName("FieldStringUninterpreted"),
        AzS1SchemaFieldStringUninterpreted::new
      ),
      Map.entry(
        AzS1Names.qName("FieldTimestamp"),
        AzS1SchemaFieldTimestamp::new
      ),
      Map.entry(
        AzS1Names.qName("FieldURI"),
        AzS1SchemaFieldURI::new
      ),
      Map.entry(
        AzS1Names.qName("FieldUUID"),
        AzS1SchemaFieldUUID::new
      )
    );
  }

  @Override
  public AzSchema onElementFinished(
    final BTElementParsingContextType context)
  {
    return new AzSchema(
      this.id,
      this.fields
    );
  }
}
