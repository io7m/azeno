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
import com.io7m.azeno.model.AzAssets;
import com.io7m.blackthorne.core.BTElementHandlerConstructorType;
import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;
import com.io7m.blackthorne.core.BTQualifiedName;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.Map;

/**
 * A parser.
 */

public final class AzA1Assets
  implements BTElementHandlerType<AzAsset1NType, AzAsset1NType>
{
  private final ArrayList<AzAsset> assets;

  /**
   * The root asset parser.
   *
   * @param context The context
   */

  public AzA1Assets(
    final BTElementParsingContextType context)
  {
    this.assets =
      new ArrayList<>();
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {

  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final AzAsset1NType result)
  {
    switch (result) {
      case final AzAsset azAsset -> {
        this.assets.add(azAsset);
      }
      case final AzAssets azAssets -> {
        this.assets.addAll(azAssets.assets());
      }
    }
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends AzAsset1NType>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        AzA1Names.qName("Asset"),
        AzA1Asset::new
      )
    );
  }

  @Override
  public AzAsset1NType onElementFinished(
    final BTElementParsingContextType context)
  {
    return new AzAssets(this.assets);
  }
}
