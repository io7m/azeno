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


package com.io7m.azeno.model;

import com.io7m.lanark.core.RDottedName;

import java.util.Locale;
import java.util.Objects;

/**
 * A text field with a locale.
 *
 * @param name        The field name
 * @param locale      The locale
 * @param multiValued Whether the field is multi-valued
 */

public record AzSchemaFieldStringLocal(
  RDottedName name,
  Locale locale,
  boolean multiValued)
  implements AzSchemaFieldTextType
{
  /**
   * A text field with a locale.
   *
   * @param name        The field name
   * @param locale      The locale
   * @param multiValued Whether the field is multi-valued
   */

  public AzSchemaFieldStringLocal
  {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(locale, "locale");

    if (locale.getISO3Language().isEmpty()) {
      throw new IllegalArgumentException(
        "Must use a locale with a supported ISO3 language code."
      );
    }
  }
}
