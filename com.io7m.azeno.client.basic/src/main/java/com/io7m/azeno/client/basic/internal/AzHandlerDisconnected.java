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

package com.io7m.azeno.client.basic.internal;

import com.io7m.azeno.client.api.AzClientConfiguration;
import com.io7m.azeno.client.api.AzClientConnectionParameters;
import com.io7m.azeno.client.api.AzClientException;
import com.io7m.azeno.client.api.AzClientTransferStatistics;
import com.io7m.azeno.model.AzAssetID;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.protocol.asset.AzACommandType;
import com.io7m.azeno.protocol.asset.AzAMessageType;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.hibiscus.api.HBClientHandlerType;
import com.io7m.hibiscus.api.HBConnectionError;
import com.io7m.hibiscus.api.HBConnectionResultType;
import com.io7m.hibiscus.api.HBTransportClosed;
import com.io7m.hibiscus.api.HBTransportType;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The initial "disconnected" protocol handler.
 */

final class AzHandlerDisconnected extends AzHandlerAbstract
{
  private final HBTransportType<AzAMessageType, AzClientException> transport;
  private final Supplier<HttpClient> httpClients;

  /**
   * Construct a handler.
   *
   * @param inConfiguration The configuration
   * @param inStrings       The string resources
   * @param inHttpClients    The client supplier
   */

  AzHandlerDisconnected(
    final AzClientConfiguration inConfiguration,
    final AzStrings inStrings,
    final Supplier<HttpClient> inHttpClients)
  {
    super(inConfiguration, inStrings);

    this.transport =
      new HBTransportClosed<>(AzClientException::ofException);
    this.httpClients =
      Objects.requireNonNull(inHttpClients, "inHttpClients");
  }

  @Override
  public HBConnectionResultType<
      AzAMessageType,
      AzClientConnectionParameters,
      HBClientHandlerType<
        AzAMessageType,
        AzClientConnectionParameters,
        AzClientException>,
      AzClientException>
  doConnect(
    final AzClientConnectionParameters parameters)
    throws InterruptedException
  {
    try {
      final var client =
        this.httpClients.get();

      final var newTransport =
        AzProtocolNegotiation.negotiateTransport(
          this.configuration(),
          parameters,
          client,
          this.strings()
        );

      final var newHandler =
        new AzHandlerConnected(
          this.configuration(),
          this.strings(),
          newTransport
        );

      return newHandler.doConnect(parameters);
    } catch (final AzClientException e) {
      return new HBConnectionError<>(e);
    }
  }

  @Override
  public HBTransportType<AzAMessageType, AzClientException> transport()
  {
    return this.transport;
  }

  @Override
  public boolean isClosed()
  {
    return this.transport.isClosed();
  }

  @Override
  public void close()
    throws AzClientException
  {
    this.transport.close();
  }

  @Override
  public String toString()
  {
    return "[%s 0x%s]".formatted(
      this.getClass().getSimpleName(),
      Integer.toUnsignedString(this.hashCode(), 16)
    );
  }

  @Override
  public Optional<AzUserID> userId()
  {
    return Optional.empty();
  }

  @Override
  public Path onExecuteFileDownload(
    final AzAssetID fileID,
    final Path file,
    final Path fileTmp,
    final long size,
    final String hashAlgorithm,
    final String hashValue,
    final Consumer<AzClientTransferStatistics> statistics)
    throws AzClientException
  {
    throw super.onNotConnected();
  }

  @Override
  public void onExecuteFileUpload(
    final AzAssetID fileID,
    final Path file,
    final String contentType,
    final String description,
    final Consumer<AzClientTransferStatistics> statistics)
    throws AzClientException
  {
    throw super.onNotConnected();
  }

  @Override
  public List<AzAResponseType> transaction(
    final List<AzACommandType<?>> commands)
    throws AzClientException
  {
    throw super.onNotConnected();
  }
}
