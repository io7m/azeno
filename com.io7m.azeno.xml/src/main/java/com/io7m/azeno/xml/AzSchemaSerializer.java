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

import com.io7m.anethum.api.SerializationException;
import com.io7m.azeno.model.AzSchema;
import com.io7m.azeno.model.AzSchemaFieldBoolean;
import com.io7m.azeno.model.AzSchemaFieldFloating;
import com.io7m.azeno.model.AzSchemaFieldInteger;
import com.io7m.azeno.model.AzSchemaFieldStringLocal;
import com.io7m.azeno.model.AzSchemaFieldStringUninterpreted;
import com.io7m.azeno.model.AzSchemaFieldTextType;
import com.io7m.azeno.model.AzSchemaFieldTimestamp;
import com.io7m.azeno.model.AzSchemaFieldType;
import com.io7m.azeno.model.AzSchemaFieldURI;
import com.io7m.azeno.model.AzSchemaFieldUUID;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

final class AzSchemaSerializer
  implements AzSchemaSerializerType
{
  private final OutputStream stream;
  private final XMLStreamWriter output;

  AzSchemaSerializer(
    final URI inTarget,
    final OutputStream inStream)
  {
    Objects.requireNonNull(inTarget, "target");

    this.stream =
      Objects.requireNonNull(inStream, "stream");

    try {
      this.output =
        XMLOutputFactory.newFactory()
          .createXMLStreamWriter(this.stream, "UTF-8");
    } catch (final XMLStreamException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String findNS()
  {
    return AzSchemas.schema1().namespace().toString();
  }

  @Override
  public String toString()
  {
    return "[AzSchemaSerializer 0x%x]"
      .formatted(Integer.valueOf(this.hashCode()));
  }

  @Override
  public void execute(
    final AzSchema value)
    throws SerializationException
  {
    try {
      this.output.writeStartDocument("UTF-8", "1.0");
      this.serializeFile(value);
      this.output.writeEndDocument();
    } catch (final XMLStreamException e) {
      throw new SerializationException(e.getMessage(), e);
    }
  }

  private void serializeFile(
    final AzSchema value)
    throws XMLStreamException
  {
    this.output.writeStartElement("Schema");
    this.output.writeDefaultNamespace(findNS());

    this.output.writeAttribute(
      "Name", value.id().name().value());
    this.output.writeAttribute(
      "Version", Integer.toUnsignedString(value.id().version()));

    final var entries =
      value.fieldTypes()
        .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .toList();

    for (final var entry : entries) {
      this.serializeField(entry.getValue());
    }

    this.output.writeEndElement();
  }

  private void serializeField(
    final AzSchemaFieldType<?> field)
    throws XMLStreamException
  {
    switch (field) {
      case final AzSchemaFieldBoolean f -> {
        this.output.writeStartElement("FieldBoolean");
        this.output.writeAttribute(
          "Name",
          f.name().value()
        );
        this.output.writeAttribute(
          "MultiValued",
          Boolean.toString(f.multiValued()));
        this.output.writeEndElement();
      }
      case final AzSchemaFieldFloating f -> {
        this.output.writeStartElement("FieldFloating");
        this.output.writeAttribute(
          "Name",
          f.name().value()
        );
        this.output.writeAttribute(
          "MultiValued",
          Boolean.toString(f.multiValued()));
        this.output.writeEndElement();
      }
      case final AzSchemaFieldInteger f -> {
        this.output.writeStartElement("FieldInteger");
        this.output.writeAttribute(
          "Name",
          f.name().value()
        );
        this.output.writeAttribute(
          "MultiValued",
          Boolean.toString(f.multiValued()));
        this.output.writeEndElement();
      }
      case final AzSchemaFieldTextType f -> {
        this.serializeFieldText(f);
      }
      case final AzSchemaFieldTimestamp f -> {
        this.output.writeStartElement("FieldTimestamp");
        this.output.writeAttribute(
          "Name",
          f.name().value()
        );
        this.output.writeAttribute(
          "MultiValued",
          Boolean.toString(f.multiValued()));
        this.output.writeEndElement();
      }

      case final AzSchemaFieldURI f -> {
        this.output.writeStartElement("FieldURI");
        this.output.writeAttribute(
          "Name",
          f.name().value()
        );
        this.output.writeAttribute(
          "MultiValued",
          Boolean.toString(f.multiValued()));
        this.output.writeEndElement();
      }

      case final AzSchemaFieldUUID f -> {
        this.output.writeStartElement("FieldUUID");
        this.output.writeAttribute(
          "Name",
          f.name().value()
        );
        this.output.writeAttribute(
          "MultiValued",
          Boolean.toString(f.multiValued()));
        this.output.writeEndElement();
      }
    }
  }

  private void serializeFieldText(
    final AzSchemaFieldTextType f)
    throws XMLStreamException
  {
    switch (f) {
      case final AzSchemaFieldStringLocal ff -> {
        this.output.writeStartElement("FieldStringLocal");
        this.output.writeAttribute(
          "Name",
          f.name().value()
        );
        this.output.writeAttribute(
          "Language",
          ff.locale().getISO3Language()
        );
        this.output.writeAttribute(
          "MultiValued",
          Boolean.toString(f.multiValued()));
        this.output.writeEndElement();
      }
      case final AzSchemaFieldStringUninterpreted ignored -> {
        this.output.writeStartElement("FieldStringUninterpreted");
        this.output.writeAttribute(
          "Name",
          f.name().value()
        );
        this.output.writeAttribute(
          "MultiValued",
          Boolean.toString(f.multiValued()));
        this.output.writeEndElement();
      }
    }
  }

  @Override
  public void close()
    throws IOException
  {
    this.stream.close();
  }
}
