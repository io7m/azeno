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
import com.io7m.azeno.client.api.AzClientType;
import com.io7m.azeno.model.AzAssetID;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.protocol.asset.AzACommandType;
import com.io7m.azeno.protocol.asset.AzAMessageType;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.hibiscus.api.HBClientAbstract;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The synchronous client.
 */

public final class AzClient
  extends HBClientAbstract<
  AzAMessageType,
  AzClientConnectionParameters,
  AzClientException>
  implements AzClientType
{
  /**
   * The client.
   *
   * @param inConfiguration The configuration
   * @param inHttpClients   The HTTP clients
   * @param inStrings       The string resources
   */

  public AzClient(
    final AzClientConfiguration inConfiguration,
    final AzStrings inStrings,
    final Supplier<HttpClient> inHttpClients)
  {
    super(
      new AzHandlerDisconnected(inConfiguration, inStrings, inHttpClients)
    );
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
    return ((AzHandlerType) this.handler()).userId();
  }

  @Override
  public void assetDownload(
    final AzAssetID assetID,
    final Path asset,
    final Path assetTmp,
    final long size,
    final String hashAlgorithm,
    final String hashValue,
    final Consumer<AzClientTransferStatistics> statistics)
    throws InterruptedException, AzClientException
  {
    ((AzHandlerType) this.handler())
      .onExecuteFileDownload(
        assetID,
        asset,
        assetTmp,
        size,
        hashAlgorithm,
        hashValue,
        statistics
      );
  }

  @Override
  public void assetUpload(
    final AzAssetID assetID,
    final Path asset,
    final String contentType,
    final String description,
    final Consumer<AzClientTransferStatistics> statistics)
    throws InterruptedException, AzClientException
  {
    ((AzHandlerType) this.handler())
      .onExecuteFileUpload(
        assetID,
        asset,
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
    return ((AzHandlerType) this.handler())
      .transaction(commands);
  }
}
