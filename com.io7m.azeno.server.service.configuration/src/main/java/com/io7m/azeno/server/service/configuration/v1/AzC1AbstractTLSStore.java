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


package com.io7m.azeno.server.service.configuration.v1;

import com.io7m.azeno.tls.AzTLSStoreConfiguration;
import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;
import org.xml.sax.Attributes;

import java.nio.file.Path;
import java.util.Objects;

abstract class AzC1AbstractTLSStore
  implements BTElementHandlerType<Object, AzC1StoreConfiguration>
{
  private final String semantic;
  private AzTLSStoreConfiguration result;

  AzC1AbstractTLSStore(
    final String inSemantic,
    final BTElementParsingContextType context)
  {
    this.semantic =
      Objects.requireNonNull(inSemantic, "semantic");
  }

  @Override
  public final void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws Exception
  {
    this.result =
      new AzTLSStoreConfiguration(
        attributes.getValue("Type"),
        attributes.getValue("Provider"),
        attributes.getValue("Password"),
        Path.of(attributes.getValue("File"))
      );
  }

  @Override
  public final AzC1StoreConfiguration onElementFinished(
    final BTElementParsingContextType context)
    throws Exception
  {
    return new AzC1StoreConfiguration(this.semantic, this.result);
  }
}
