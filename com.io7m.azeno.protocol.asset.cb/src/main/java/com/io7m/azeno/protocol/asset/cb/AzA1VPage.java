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


package com.io7m.azeno.protocol.asset.cb;

import com.io7m.azeno.model.AzPage;
import com.io7m.cedarbridge.runtime.api.CBIntegerUnsigned32;
import com.io7m.cedarbridge.runtime.api.CBIntegerUnsigned64;
import com.io7m.cedarbridge.runtime.api.CBSerializableType;
import com.io7m.cedarbridge.runtime.convenience.CBLists;

import java.util.function.Function;

import static java.lang.Integer.toUnsignedLong;

/**
 * A validator.
 */

public final class AzA1VPage
{
  private AzA1VPage()
  {

  }

  /**
   * Convert a page from wire format.
   *
   * @param page The input
   * @param f    The value transformer
   * @param <A>  The type of page wire values
   * @param <B>  The type of result values
   *
   * @return A page
   */

  public static <A extends CBSerializableType, B> AzPage<B> pageFromWire(
    final AzA1Page<A> page,
    final Function<A, B> f)
  {
    return new AzPage<>(
      page.fieldItems().values().stream().map(f).toList(),
      (int) page.fieldPageIndex().value(),
      (int) page.fieldPageCount().value(),
      page.fieldPageFirstOffset().value()
    );
  }

  /**
   * Convert a page to wire format.
   *
   * @param data The input
   * @param f    The value transformer
   * @param <A>  The type of result values
   * @param <B>  The type of page wire values
   *
   * @return A page
   */

  public static <A, B extends CBSerializableType> AzA1Page<B> pageToWire(
    final AzPage<A> data,
    final Function<A, B> f)
  {
    return new AzA1Page<>(
      CBLists.ofCollection(data.items(), f),
      new CBIntegerUnsigned32(toUnsignedLong(data.pageIndex())),
      new CBIntegerUnsigned32(toUnsignedLong(data.pageCount())),
      new CBIntegerUnsigned64(data.pageFirstOffset())
    );
  }
}
