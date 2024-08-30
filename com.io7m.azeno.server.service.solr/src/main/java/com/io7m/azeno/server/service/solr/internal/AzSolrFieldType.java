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


package com.io7m.azeno.server.service.solr.internal;

import java.util.Objects;

/**
 * A schema field type definition.
 *
 * @param name        The field name
 * @param className   The field class (such as "solr.UUIDField")
 * @param multiValued Whether the field is multi-valued
 */

public record AzSolrFieldType(
  String name,
  String className,
  boolean multiValued)
{
  /**
   * A schema field type definition.
   *
   * @param name        The field name
   * @param className   The field class (such as "solr.UUIDField")
   * @param multiValued Whether the field is multi-valued
   */

  public AzSolrFieldType
  {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(className, "className");
  }
}
