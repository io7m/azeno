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


package com.io7m.azeno.protocol.asset.cb;

import com.io7m.azeno.model.AzComparisonExactType;
import com.io7m.azeno.protocol.api.AzProtocolException;
import com.io7m.azeno.protocol.api.AzProtocolMessageValidatorType;
import com.io7m.cedarbridge.runtime.api.CBSerializableType;

import java.util.Objects;

/**
 * Exact comparisons.
 *
 * @param <V> The type of model values
 * @param <W> The type of serialized values
 */

public final class AzA1VComparisonsExact<V, W extends CBSerializableType>
  implements AzProtocolMessageValidatorType<
  AzComparisonExactType<V>,
  AzA1ComparisonExact<W>>
{
  private final AzProtocolMessageValidatorType<V, W> validator;

  /**
   * Exact comparisons.
   *
   * @param inValidator A validator for values
   */

  public AzA1VComparisonsExact(
    final AzProtocolMessageValidatorType<V, W> inValidator)
  {
    this.validator =
      Objects.requireNonNull(inValidator, "validator");
  }

  @Override
  public AzA1ComparisonExact<W> convertToWire(
    final AzComparisonExactType<V> message)
    throws AzProtocolException
  {
    return switch (message) {
      case final AzComparisonExactType.Anything<V> e -> {
        yield new AzA1ComparisonExact.Anything<>();
      }
      case final AzComparisonExactType.IsEqualTo<V> e -> {
        yield new AzA1ComparisonExact.IsEqualTo<>(
          this.validator.convertToWire(e.value())
        );
      }
      case final AzComparisonExactType.IsNotEqualTo<V> e -> {
        yield new AzA1ComparisonExact.IsNotEqualTo<>(
          this.validator.convertToWire(e.value())
        );
      }
    };
  }

  @Override
  public AzComparisonExactType<V> convertFromWire(
    final AzA1ComparisonExact<W> message)
    throws AzProtocolException
  {
    return switch (message) {
      case final AzA1ComparisonExact.Anything<W> e -> {
        yield new AzComparisonExactType.Anything<>();
      }
      case final AzA1ComparisonExact.IsEqualTo<W> e -> {
        yield new AzComparisonExactType.IsEqualTo<>(
          this.validator.convertFromWire(e.fieldValue())
        );
      }
      case final AzA1ComparisonExact.IsNotEqualTo<W> e -> {
        yield new AzComparisonExactType.IsNotEqualTo<>(
          this.validator.convertFromWire(e.fieldValue())
        );
      }
    };
  }
}
