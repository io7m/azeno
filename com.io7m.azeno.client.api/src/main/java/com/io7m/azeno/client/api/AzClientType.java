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

package com.io7m.azeno.client.api;

import com.io7m.azeno.model.AzAssetID;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.protocol.asset.AzACommandType;
import com.io7m.azeno.protocol.asset.AzAMessageType;
import com.io7m.azeno.protocol.asset.AzAResponseError;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.hibiscus.api.HBClientType;
import com.io7m.hibiscus.api.HBConnectionError;
import com.io7m.hibiscus.api.HBConnectionFailed;
import com.io7m.hibiscus.api.HBConnectionParametersType;
import com.io7m.hibiscus.api.HBConnectionSucceeded;
import com.io7m.hibiscus.api.HBMessageType;
import com.io7m.repetoir.core.RPServiceType;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * The type of client instances.
 */

public interface AzClientType
  extends HBClientType<
  AzAMessageType,
  AzClientConnectionParameters,
  AzClientException>,
  RPServiceType
{
  @Override
  default String description()
  {
    return "A synchronous asset client.";
  }

  /**
   * @return The current logged-in user
   */

  Optional<AzUserID> userId();

  /**
   * Download the data associated with the given asset. The asset will be
   * downloaded to a temporary asset and then, if everything succeeds and the
   * hash value matches, the temporary asset will atomically replace the
   * output asset.
   *
   * @param assetID        The asset ID
   * @param asset          The output asset
   * @param assetTmp       The temporary output asset
   * @param size          The expected size
   * @param hashAlgorithm The hash algorithm
   * @param hashValue     The expected hash value
   * @param statistics    A receiver of transfer statistics
   *
   * @throws AzClientException    On errors
   * @throws InterruptedException On interruption
   */

  void assetDownload(
    AzAssetID assetID,
    Path asset,
    Path assetTmp,
    long size,
    String hashAlgorithm,
    String hashValue,
    Consumer<AzClientTransferStatistics> statistics)
    throws InterruptedException, AzClientException;

  /**
   * Upload the data associated with the given asset.
   *
   * @param assetID      The asset ID
   * @param asset        The input asset
   * @param contentType The content type
   * @param description The asset description
   * @param statistics  A receiver of transfer statistics
   *
   * @throws AzClientException    On errors
   * @throws InterruptedException On interruption
   */

  void assetUpload(
    AzAssetID assetID,
    Path asset,
    String contentType,
    String description,
    Consumer<AzClientTransferStatistics> statistics)
    throws InterruptedException, AzClientException;

  /**
   * Call {@link #connect(HBConnectionParametersType)} but throw an exception
   * if the result is an {@link AzAResponseError}.
   *
   * @param parameters The connection parameters
   *
   * @return The success message
   *
   * @throws AzClientException    On errors
   * @throws InterruptedException On interruption
   */

  default AzAMessageType connectOrThrow(
    final AzClientConnectionParameters parameters)
    throws AzClientException, InterruptedException
  {
    final var r =
      this.connect(parameters);

    return switch (r) {
      case final HBConnectionError<
        AzAMessageType, AzClientConnectionParameters, ?, AzClientException>
        error -> {
        throw AzClientException.ofException(error.exception());
      }
      case final HBConnectionFailed<
        AzAMessageType, AzClientConnectionParameters, ?, AzClientException>
        failed -> {
        if (failed.message() instanceof final AzAResponseError error) {
          throw AzClientException.ofError(error);
        }
        throw new IllegalStateException();
      }
      case final HBConnectionSucceeded<
        AzAMessageType, AzClientConnectionParameters, ?, AzClientException>
        succeeded -> {
        yield succeeded.message();
      }
    };
  }

  /**
   * Call {@link #sendAndWait(HBMessageType, Duration)} but throw an exception
   * if the result is an {@link AzAResponseError}.
   *
   * @param message The message
   * @param timeout The timeout
   * @param <R>     The type of results
   *
   * @return The result
   *
   * @throws AzClientException    On errors
   * @throws InterruptedException On interruption
   * @throws TimeoutException     On timeouts
   */

  default <R extends AzAResponseType> R sendAndWaitOrThrow(
    final AzACommandType<R> message,
    final Duration timeout)
    throws AzClientException, InterruptedException, TimeoutException
  {
    final var r =
      this.sendAndWait(message, timeout);

    return switch (r) {
      case final AzAResponseError error -> {
        throw AzClientException.ofError(error);
      }
      default -> (R) r;
    };
  }

  /**
   * Execute all the given commands in order, in a single transaction.
   *
   * @param commands The commands
   *
   * @return The responses
   */

  List<AzAResponseType> transaction(
    List<AzACommandType<?>> commands)
    throws AzClientException, InterruptedException;
}
