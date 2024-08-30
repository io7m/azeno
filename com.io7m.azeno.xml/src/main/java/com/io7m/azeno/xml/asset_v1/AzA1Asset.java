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


package com.io7m.azeno.xml.asset_v1;

import com.io7m.azeno.model.AzAsset;
import com.io7m.azeno.model.AzAsset1NType;
import com.io7m.azeno.model.AzAssetID;
import com.io7m.azeno.model.AzAssetProperties;
import com.io7m.azeno.model.AzCollectionID;
import com.io7m.azeno.model.AzHashSHA256;
import com.io7m.azeno.model.AzValueType;
import com.io7m.blackthorne.core.BTElementHandlerConstructorType;
import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;
import com.io7m.blackthorne.core.BTQualifiedName;
import org.xml.sax.Attributes;

import java.util.Map;

/**
 * A parser.
 */

public final class AzA1Asset
  implements BTElementHandlerType<AzValueType, AzAsset1NType>
{
  private final AzAssetProperties.Builder properties;
  private AzAssetID id;
  private String hashAlgorithm;
  private String hashValue;
  private AzCollectionID collection;

  /**
   * The root asset parser.
   *
   * @param context The context
   */

  public AzA1Asset(
    final BTElementParsingContextType context)
  {
    this.properties =
      AzAssetProperties.builder();
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.id =
      AzAssetID.of(attributes.getValue("ID"));
    this.collection =
      AzCollectionID.of(attributes.getValue("Collection"));
    this.hashAlgorithm =
      attributes.getValue("HashAlgorithm");
    this.hashValue =
      attributes.getValue("HashValue");
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final AzValueType result)
  {
    this.properties.put(result);
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends AzValueType>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        AzA1Names.qName("ValueBoolean"),
        AzA1ValueBoolean::new
      ),
      Map.entry(
        AzA1Names.qName("ValueFloating"),
        AzA1ValueFloating::new
      ),
      Map.entry(
        AzA1Names.qName("ValueInteger"),
        AzA1ValueInteger::new
      ),
      Map.entry(
        AzA1Names.qName("ValueString"),
        AzA1ValueString::new
      ),
      Map.entry(
        AzA1Names.qName("ValueTimestamp"),
        AzA1ValueTimestamp::new
      ),
      Map.entry(
        AzA1Names.qName("ValueURI"),
        AzA1ValueURI::new
      ),
      Map.entry(
        AzA1Names.qName("ValueUUID"),
        AzA1ValueUUID::new
      )
    );
  }

  @Override
  public AzAsset1NType onElementFinished(
    final BTElementParsingContextType context)
  {
    return new AzAsset(
      this.id,
      this.collection,
      new AzHashSHA256(this.hashValue),
      this.properties.build()
    );
  }
}
