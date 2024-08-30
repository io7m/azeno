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
import com.io7m.azeno.error_codes.AzStandardErrorCodes;
import com.io7m.azeno.model.AzAssetID;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.protocol.asset.AzACommandLogin;
import com.io7m.azeno.protocol.asset.AzACommandType;
import com.io7m.azeno.protocol.asset.AzAMessageType;
import com.io7m.azeno.protocol.asset.AzAResponseError;
import com.io7m.azeno.protocol.asset.AzAResponseLogin;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.hibiscus.api.HBClientHandlerType;
import com.io7m.hibiscus.api.HBConnectionError;
import com.io7m.hibiscus.api.HBConnectionFailed;
import com.io7m.hibiscus.api.HBConnectionResultType;
import com.io7m.hibiscus.api.HBConnectionSucceeded;
import com.io7m.hibiscus.api.HBTransportType;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

final class AzHandlerConnected
  extends AzHandlerAbstract
{
  private final AzTransportType transport;
  private AzACommandLogin mostRecentLogin;
  private Duration loginTimeout;
  private AzUserID userId;

  AzHandlerConnected(
    final AzClientConfiguration inConfiguration,
    final AzStrings inStrings,
    final AzTransportType inTransport)
  {
    super(inConfiguration, inStrings);

    this.transport =
      Objects.requireNonNull(inTransport, "transport");
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
    final var commandLogin =
      new AzACommandLogin(
        parameters.username(),
        parameters.password(),
        parameters.metadata()
      );

    this.loginTimeout = parameters.loginTimeout();
    try {
      return this.doLogin(commandLogin);
    } catch (final AzClientException | TimeoutException e) {
      return new HBConnectionError<>(e);
    }
  }

  @Override
  public AzAMessageType sendAndWait(
    final AzAMessageType message,
    final Duration timeout)
    throws AzClientException, InterruptedException, TimeoutException
  {
    var attempt = 0;

    while (true) {
      ++attempt;

      final var response =
        this.transport.sendAndWait(message, timeout);

      if (response instanceof final AzAResponseError error) {
        if (!isAuthenticationError(error)) {
          return error;
        }

        if (attempt == 3) {
          return error;
        }

        this.doLogin(this.mostRecentLogin);
        continue;
      }

      return response;
    }
  }

  @Override
  public HBTransportType<AzAMessageType, AzClientException> transport()
  {
    return this.transport;
  }

  private HBConnectionResultType<
    AzAMessageType,
    AzClientConnectionParameters,
    HBClientHandlerType<
      AzAMessageType,
      AzClientConnectionParameters,
      AzClientException>,
    AzClientException>
  doLogin(
    final AzACommandLogin commandLogin)
    throws AzClientException,
    InterruptedException,
    TimeoutException
  {
    final var response =
      this.transport.sendAndWait(commandLogin, this.loginTimeout);

    return switch (response) {
      case final AzAResponseLogin login -> {
        this.mostRecentLogin = commandLogin;
        this.userId = login.userId();
        yield new HBConnectionSucceeded<>(login, this);
      }

      case final AzAResponseError error -> {
        yield new HBConnectionFailed<>(error);
      }

      default -> {
        yield new HBConnectionFailed<>(response);
      }
    };
  }

  private static boolean isAuthenticationError(
    final AzAResponseError error)
  {
    return Objects.equals(
      error.errorCode(),
      AzStandardErrorCodes.errorAuthentication()
    );
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
    return Optional.ofNullable(this.userId);
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
    throws InterruptedException, AzClientException
  {
    Objects.requireNonNull(fileID, "fileID");
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(fileTmp, "fileTmp");
    Objects.requireNonNull(hashAlgorithm, "hashAlgorithm");
    Objects.requireNonNull(hashValue, "hashValue");
    Objects.requireNonNull(statistics, "statistics");

    return this.transport.fileDownload(
      fileID,
      file,
      fileTmp,
      size,
      hashAlgorithm,
      hashValue,
      statistics
    );
  }

  @Override
  public void onExecuteFileUpload(
    final AzAssetID fileID,
    final Path file,
    final String contentType,
    final String description,
    final Consumer<AzClientTransferStatistics> statistics)
    throws InterruptedException, AzClientException
  {
    Objects.requireNonNull(fileID, "fileID");
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(contentType, "fileTmp");
    Objects.requireNonNull(description, "hashAlgorithm");
    Objects.requireNonNull(statistics, "statistics");

    this.transport.fileUpload(
      fileID,
      file,
      contentType,
      description,
      statistics
    );
  }

  @Override
  public List<AzAResponseType> transaction(
    final List<AzACommandType<?>> commands)
    throws AzClientException, InterruptedException
  {
    return this.transport.transaction(commands);
  }
}
