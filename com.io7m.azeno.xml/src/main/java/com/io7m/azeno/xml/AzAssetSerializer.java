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
import com.io7m.azeno.model.AzAsset;
import com.io7m.azeno.model.AzAsset1NType;
import com.io7m.azeno.model.AzAssets;
import com.io7m.azeno.model.AzHashSHA256;
import com.io7m.azeno.model.AzValueBoolean;
import com.io7m.azeno.model.AzValueFloating;
import com.io7m.azeno.model.AzValueInteger;
import com.io7m.azeno.model.AzValueString;
import com.io7m.azeno.model.AzValueTimestamp;
import com.io7m.azeno.model.AzValueType;
import com.io7m.azeno.model.AzValueURI;
import com.io7m.azeno.model.AzValueUUID;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class AzAssetSerializer
  implements AzAssetSerializerType
{
  private final OutputStream stream;
  private final XMLStreamWriter output;

  AzAssetSerializer(
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
    return AzSchemas.asset1().namespace().toString();
  }

  @Override
  public String toString()
  {
    return "[AzAssetSerializer 0x%x]"
      .formatted(Integer.valueOf(this.hashCode()));
  }

  @Override
  public void execute(
    final AzAsset1NType value)
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
    final AzAsset1NType value)
    throws XMLStreamException
  {
    switch (value) {
      case final AzAsset azAsset -> {
        this.serializeAsset(azAsset, true);
      }
      case final AzAssets azAssets -> {
        this.serializeAssets(azAssets, true);
      }
    }
  }

  private void serializeAssets(
    final AzAssets azAssets,
    final boolean root)
    throws XMLStreamException
  {
    this.output.writeStartElement("Assets");

    if (root) {
      this.output.writeDefaultNamespace(findNS());
    }

    for (final var asset : azAssets.assets()) {
      this.serializeAsset(asset, false);
    }

    this.output.writeEndElement();
  }

  private void serializeAsset(
    final AzAsset azAsset,
    final boolean root)
    throws XMLStreamException
  {
    this.output.writeStartElement("Asset");

    if (root) {
      this.output.writeDefaultNamespace(findNS());
    }

    this.output.writeAttribute("ID", azAsset.id().toString());
    this.output.writeAttribute("Collection", azAsset.collection().toString());

    switch (azAsset.hash()) {
      case final AzHashSHA256 h -> {
        this.output.writeAttribute("HashAlgorithm", "SHA-256");
        this.output.writeAttribute("HashValue", h.value());
      }
    }

    final var entries =
      azAsset.properties()
        .values()
        .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .toList();

    for (final var entry : entries) {
      this.serializeValues(entry.getValue());
    }

    this.output.writeEndElement();
  }

  private void serializeValues(
    final List<AzValueType> values)
    throws XMLStreamException
  {
    for (final var value : values) {
      this.serializeValue(value);
    }
  }

  private void serializeValue(
    final AzValueType value)
    throws XMLStreamException
  {
    switch (value) {
      case final AzValueBoolean v -> {
        this.output.writeStartElement("ValueBoolean");
        this.output.writeAttribute("Name", v.name().value());
        this.output.writeAttribute("Value", Boolean.toString(v.value()));
        this.output.writeEndElement();
      }
      case final AzValueFloating v -> {
        this.output.writeStartElement("ValueFloating");
        this.output.writeAttribute("Name", v.name().value());
        this.output.writeAttribute("Value", Double.toString(v.value()));
        this.output.writeEndElement();
      }
      case final AzValueInteger v -> {
        this.output.writeStartElement("ValueInteger");
        this.output.writeAttribute("Name", v.name().value());
        this.output.writeAttribute("Value", v.value().toString());
        this.output.writeEndElement();
      }
      case final AzValueString v -> {
        this.output.writeStartElement("ValueString");
        this.output.writeAttribute("Name", v.name().value());
        this.output.writeCharacters(v.value());
        this.output.writeEndElement();
      }
      case final AzValueTimestamp v -> {
        final var timeText =
          DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(v.value());

        this.output.writeStartElement("ValueTimestamp");
        this.output.writeAttribute("Name", v.name().value());
        this.output.writeAttribute("Value", timeText);
        this.output.writeEndElement();
      }
      case final AzValueURI v -> {
        this.output.writeStartElement("ValueURI");
        this.output.writeAttribute("Name", v.name().value());
        this.output.writeAttribute("Value", v.value().toString());
        this.output.writeEndElement();
      }
      case final AzValueUUID v -> {
        this.output.writeStartElement("ValueUUID");
        this.output.writeAttribute("Name", v.name().value());
        this.output.writeAttribute("Value", v.value().toString());
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
