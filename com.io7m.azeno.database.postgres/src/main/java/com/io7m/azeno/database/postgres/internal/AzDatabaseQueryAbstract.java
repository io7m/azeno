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


package com.io7m.azeno.database.postgres.internal;

import com.io7m.azeno.database.api.AzDatabaseQueryType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.model.AzAuditEvent;
import com.io7m.darco.api.DDatabaseQueryAbstract;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.postgres.extensions.bindings.HstoreBinding;
import org.jooq.postgres.extensions.types.Hstore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.io7m.azeno.database.postgres.internal.Tables.AUDIT;

/**
 * The abstract type of queries.
 *
 * @param <P> The type of parameters
 * @param <R> The type of results
 */

public abstract class AzDatabaseQueryAbstract<P, R>
  extends DDatabaseQueryAbstract<AzDatabaseTransactionType, P, R>
  implements AzDatabaseQueryType<P, R>
{
  private static final DataType<Hstore> AU_DATA_TYPE =
    SQLDataType.OTHER.asConvertedDataType(new HstoreBinding());

  static final Field<Hstore> AU_DATA =
    DSL.field("AUDIT_DATA", AU_DATA_TYPE);

  private final HashMap<String, String> attributes;

  protected AzDatabaseQueryAbstract(
    final AzDatabaseTransactionType t)
  {
    super(t);
    this.attributes = new HashMap<String, String>();
  }

  protected final void putAttribute(
    final String name,
    final Object value)
  {
    this.attributes.put(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(value, "value").toString()
    );
  }

  protected final Map<String, String> attributes()
  {
    return Map.copyOf(this.attributes);
  }

  static Query putAuditEvent(
    final DSLContext context,
    final AzAuditEvent event)
  {
    return context.insertInto(AUDIT)
      .set(AUDIT.AUDIT_TYPE, event.type())
      .set(AUDIT.AUDIT_TIME, event.time())
      .set(AUDIT.AUDIT_USER_ID, event.owner().id())
      .set(AU_DATA, Hstore.valueOf(event.data()));
  }
}
