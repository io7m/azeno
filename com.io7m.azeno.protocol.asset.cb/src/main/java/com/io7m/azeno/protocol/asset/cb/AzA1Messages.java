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

import com.io7m.azeno.error_codes.AzErrorCode;
import com.io7m.azeno.protocol.api.AzProtocolException;
import com.io7m.azeno.protocol.api.AzProtocolMessagesType;
import com.io7m.azeno.protocol.asset.AzACommandDebugInvalid;
import com.io7m.azeno.protocol.asset.AzACommandDebugRandom;
import com.io7m.azeno.protocol.asset.AzAMessageType;
import com.io7m.azeno.protocol.asset.AzAResponseError;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.azeno.protocol.asset.AzATransactionResponse;
import com.io7m.cedarbridge.runtime.api.CBProtocolMessageVersionedSerializerType;
import com.io7m.cedarbridge.runtime.api.CBSerializationContextType;
import com.io7m.cedarbridge.runtime.bssio.CBSerializationContextBSSIO;
import com.io7m.jbssio.api.BSSReaderProviderType;
import com.io7m.jbssio.api.BSSWriterProviderType;
import com.io7m.jbssio.vanilla.BSSReaders;
import com.io7m.jbssio.vanilla.BSSWriters;
import com.io7m.repetoir.core.RPServiceType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorIo;
import static com.io7m.azeno.protocol.asset.AzAResponseBlame.BLAME_CLIENT;

/**
 * The protocol messages for Digital asset Cedarbridge.
 */

public final class AzA1Messages
  implements AzProtocolMessagesType<AzAMessageType>, RPServiceType
{
  private static final ProtocolAzA PROTOCOL = new ProtocolAzA();

  /**
   * The content type for the protocol.
   */

  public static final String CONTENT_TYPE =
    "application/azeno_asset+cedarbridge";

  /**
   * The content type for the protocol.
   */

  public static final String CONTENT_TYPE_FOR_SEQUENCE =
    "application/azeno_asset_sequence+cedarbridge";

  private final BSSReaderProviderType readers;
  private final BSSWriterProviderType writers;
  private final AzA1Validation validator;
  private final CBProtocolMessageVersionedSerializerType<ProtocolAzAType> serializer;

  /**
   * The protocol messages for Admin schema_v1 Cedarbridge.
   *
   * @param inReaders The readers
   * @param inWriters The writers
   */

  public AzA1Messages(
    final BSSReaderProviderType inReaders,
    final BSSWriterProviderType inWriters)
  {
    this.readers =
      Objects.requireNonNull(inReaders, "readers");
    this.writers =
      Objects.requireNonNull(inWriters, "writers");

    this.validator = new AzA1Validation();
    this.serializer =
      PROTOCOL.serializerForProtocolVersion(1L)
        .orElseThrow(() -> {
          return new IllegalStateException("No support for version 1");
        });
  }

  /**
   * The protocol messages for Digital asset schema_v1 Cedarbridge.
   */

  public AzA1Messages()
  {
    this(new BSSReaders(), new BSSWriters());
  }

  /**
   * @return The content type
   */

  public static String contentType()
  {
    return CONTENT_TYPE;
  }

  /**
   * @return The content type for sequences
   */

  public static String contentTypeForSequence()
  {
    return CONTENT_TYPE_FOR_SEQUENCE;
  }

  /**
   * @return The protocol identifier
   */

  public static UUID protocolId()
  {
    return PROTOCOL.protocolId();
  }

  @Override
  public AzAMessageType parse(
    final byte[] data)
    throws AzProtocolException
  {
    final var context =
      CBSerializationContextBSSIO.createFromByteArray(this.readers, data);

    try {
      return this.validator.convertFromWire(
        (ProtocolAzAv1Type) this.serializer.deserialize(context)
      );
    } catch (final IOException e) {
      throw new AzProtocolException(
        e.getMessage(),
        e,
        errorIo(),
        Collections.emptySortedMap(),
        Optional.empty()
      );
    }
  }

  @Override
  public byte[] serialize(
    final AzAMessageType message)
  {
    try (var output = new ByteArrayOutputStream()) {
      final var context =
        CBSerializationContextBSSIO.createFromOutputStream(
          this.writers,
          output
        );

      switch (message) {
        case final AzACommandDebugInvalid ignored -> {
          this.serializeInvalid(context);
        }

        case final AzACommandDebugRandom ignored -> {
          serializeRandom(output);
        }

        case final AzATransactionResponse transactionResponse -> {
          this.serializeTransactionResponse(output, transactionResponse);
        }

        case null, default -> {
          this.serializer.serialize(
            context,
            this.validator.convertToWire(message)
          );
        }
      }

      return output.toByteArray();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    } catch (final AzProtocolException | NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private void serializeTransactionResponse(
    final OutputStream outputStream,
    final AzATransactionResponse transactionResponse)
    throws IOException, AzProtocolException
  {
    try (var dataOut = new DataOutputStream(outputStream)) {
      for (final AzAResponseType message : transactionResponse.responses()) {
        final var output =
          new ByteArrayOutputStream();
        final var context =
          CBSerializationContextBSSIO.createFromOutputStream(
            this.writers,
            output
          );

        this.serializer.serialize(
          context,
          this.validator.convertToWire(message)
        );

        final var data = output.toByteArray();
        dataOut.writeInt(data.length);
        dataOut.write(data);
      }

      dataOut.writeInt(0);
    }
  }

  private static void serializeRandom(
    final ByteArrayOutputStream outputStream)
    throws NoSuchAlgorithmException
  {
    final var random = SecureRandom.getInstanceStrong();
    final var data = new byte[1024];
    random.nextBytes(data);
    outputStream.writeBytes(data);
  }

  private void serializeInvalid(
    final CBSerializationContextType context)
    throws IOException, AzProtocolException
  {
    this.serializer.serialize(
      context,
      this.validator.convertToWire(new AzAResponseError(
        UUID.randomUUID(),
        "Invalid!",
        new AzErrorCode("error-invalid"),
        Map.of("X", "Y"),
        Optional.of("Avoid sending this."),
        Optional.empty(),
        BLAME_CLIENT,
        List.of()
      ))
    );
  }

  @Override
  public String description()
  {
    return "Digital asset Cedarbridge message service.";
  }

  @Override
  public String toString()
  {
    return "[AzAMessages 0x%s]"
      .formatted(Long.toUnsignedString(this.hashCode(), 16));
  }
}
