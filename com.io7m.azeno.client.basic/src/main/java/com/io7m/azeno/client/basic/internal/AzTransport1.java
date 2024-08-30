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

import com.io7m.azeno.client.api.AzClientException;
import com.io7m.azeno.client.api.AzClientTransferStatistics;
import com.io7m.azeno.error_codes.AzStandardErrorCodes;
import com.io7m.azeno.model.AzAssetID;
import com.io7m.azeno.protocol.api.AzProtocolException;
import com.io7m.azeno.protocol.asset.AzACommandLogin;
import com.io7m.azeno.protocol.asset.AzACommandType;
import com.io7m.azeno.protocol.asset.AzAMessageType;
import com.io7m.azeno.protocol.asset.AzAResponseError;
import com.io7m.azeno.protocol.asset.AzAResponseType;
import com.io7m.azeno.protocol.asset.cb.AzA1Messages;
import com.io7m.azeno.strings.AzStringConstants;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.hibiscus.api.HBReadNothing;
import com.io7m.hibiscus.api.HBReadResponse;
import com.io7m.hibiscus.api.HBReadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.ClosedChannelException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.io7m.azeno.client.basic.internal.AzCompression.decompressResponse;
import static com.io7m.azeno.client.basic.internal.AzUUIDs.nullUUID;
import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorIo;
import static com.io7m.azeno.protocol.asset.AzAResponseBlame.BLAME_CLIENT;
import static com.io7m.azeno.strings.AzStringConstants.ERROR_EXPECTED_COMMAND_TYPE;
import static com.io7m.azeno.strings.AzStringConstants.ERROR_HASH_VALUE_MISMATCH;
import static com.io7m.azeno.strings.AzStringConstants.ERROR_UNEXPECTED_CONTENT_TYPE;
import static com.io7m.azeno.strings.AzStringConstants.ERROR_UNEXPECTED_RESPONSE_TYPE;
import static com.io7m.azeno.strings.AzStringConstants.EXPECTED_CONTENT_TYPE;
import static com.io7m.azeno.strings.AzStringConstants.EXPECTED_HASH;
import static com.io7m.azeno.strings.AzStringConstants.EXPECTED_RESPONSE_TYPE;
import static com.io7m.azeno.strings.AzStringConstants.HASH_ALGORITHM;
import static com.io7m.azeno.strings.AzStringConstants.RECEIVED_CONTENT_TYPE;
import static com.io7m.azeno.strings.AzStringConstants.RECEIVED_HASH;
import static com.io7m.azeno.strings.AzStringConstants.RECEIVED_RESPONSE_TYPE;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * The version 1 transport.
 */

public final class AzTransport1
  implements AzTransportType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AzTransport1.class);

  private final AzA1Messages messages;
  private final AzStrings strings;
  private final Clock clock;
  private final HttpClient http;
  private final LinkedBlockingQueue<MessageAndResponse> inbox;
  private final URI commandURI;
  private final URI fileDownloadURI;
  private final URI fileUploadURI;
  private final URI loginURI;
  private final URI transactionURI;

  private record MessageAndResponse(
    AzAMessageType sent,
    AzAMessageType received)
  {

  }

  /**
   * The version 1 transport.
   *
   * @param inClock      The clock
   * @param inStrings    The string resources
   * @param inHttpClient The HTTP client
   * @param baseURI      The base URI
   */

  public AzTransport1(
    final Clock inClock,
    final AzStrings inStrings,
    final HttpClient inHttpClient,
    final URI baseURI)
  {
    this.clock =
      Objects.requireNonNull(inClock, "inClock");
    this.http =
      Objects.requireNonNull(inHttpClient, "inHttpClient");
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");

    this.inbox =
      new LinkedBlockingQueue<>();

    this.messages =
      new AzA1Messages();
    this.loginURI =
      baseURI.resolve("login")
        .normalize();
    this.commandURI =
      baseURI.resolve("command")
        .normalize();
    this.fileUploadURI =
      baseURI.resolve("file-upload")
        .normalize();
    this.fileDownloadURI =
      baseURI.resolve("file-download")
        .normalize();
    this.transactionURI =
      baseURI.resolve("transaction")
        .normalize();
  }

  private AzClientException errorClosed()
  {
    return new AzClientException(
      this.strings.format(AzStringConstants.ERROR_CLOSED_CHANNEL),
      new ClosedChannelException(),
      AzStandardErrorCodes.errorApiMisuse(),
      Map.of(),
      Optional.empty(),
      Optional.empty()
    );
  }

  private MessageAndResponse sendMessage(
    final AzAMessageType message,
    final URI targetURI,
    final Optional<Duration> timeout)
    throws
    AzClientException,
    IOException,
    AzProtocolException,
    InterruptedException
  {
    if (message instanceof final AzACommandType<?> command) {
      return this.sendCommand(targetURI, command, timeout);
    } else {
      throw this.errorNotCommand(message);
    }
  }

  private MessageAndResponse sendCommand(
    final URI targetURI,
    final AzACommandType<?> command,
    final Optional<Duration> timeout)
    throws
    IOException,
    InterruptedException,
    AzClientException,
    AzProtocolException
  {
    final var data =
      this.messages.serialize(command);

    final var requestBuilder =
      HttpRequest.newBuilder()
        .uri(targetURI)
        .POST(HttpRequest.BodyPublishers.ofByteArray(data));

    timeout.ifPresent(requestBuilder::timeout);

    final var response =
      this.http.send(
        requestBuilder.build(),
        HttpResponse.BodyHandlers.ofByteArray()
      );

    LOG.debug("Write: Status {}", Integer.valueOf(response.statusCode()));

    final var responseHeaders =
      response.headers();

    /*
     * Check the content type. Fail if it's not what we expected.
     */

    final var contentType =
      responseHeaders.firstValue("content-type")
        .orElse("application/octet-stream");

    final var expectedContentType = AzA1Messages.contentType();
    if (!contentType.equals(expectedContentType)) {
      throw this.errorContentType(contentType, expectedContentType);
    }

    /*
     * Parse the response message, decompressing if necessary. If the
     * parsed message isn't a response... fail.
     */

    final var responseMessage =
      this.messages.parse(decompressResponse(response, responseHeaders));

    if (!(responseMessage instanceof AzAResponseType)) {
      throw this.errorUnexpectedResponseType(command, responseMessage);
    }

    /*
     * If the response is an error, accept it.
     */

    if (responseMessage instanceof final AzAResponseError error) {
      return new MessageAndResponse(command, error);
    }

    /*
     * Otherwise, reject the response if it isn't of the correct type.
     */

    if (!Objects.equals(command.responseClass(), responseMessage.getClass())) {
      throw this.errorUnexpectedResponseType(command, responseMessage);
    }

    return new MessageAndResponse(command, responseMessage);
  }

  @Override
  public boolean isClosed()
  {
    return this.http.isTerminated();
  }

  @Override
  public void close()
  {
    this.http.close();
  }

  private AzClientException errorNotCommand(
    final AzAMessageType message)
  {
    final var attributes = new HashMap<String, String>();
    attributes.put(
      this.strings.format(AzStringConstants.MESSAGE_TYPE),
      message.getClass().getSimpleName()
    );

    return new AzClientException(
      this.strings.format(ERROR_EXPECTED_COMMAND_TYPE),
      AzStandardErrorCodes.errorProtocol(),
      Map.copyOf(attributes),
      Optional.empty(),
      Optional.empty()
    );
  }

  private AzClientException errorContentType(
    final String contentType,
    final String expectedContentType)
  {
    final var attributes = new HashMap<String, String>();
    attributes.put(
      this.strings.format(EXPECTED_CONTENT_TYPE),
      expectedContentType
    );
    attributes.put(
      this.strings.format(RECEIVED_CONTENT_TYPE),
      contentType
    );

    return new AzClientException(
      this.strings.format(ERROR_UNEXPECTED_CONTENT_TYPE),
      AzStandardErrorCodes.errorProtocol(),
      Map.copyOf(attributes),
      Optional.empty(),
      Optional.empty()
    );
  }

  private AzClientException errorUnexpectedResponseType(
    final AzAMessageType message,
    final AzAMessageType responseActual)
  {
    final var attributes = new HashMap<String, String>();
    if (message instanceof final AzACommandType<?> cmd) {
      attributes.put(
        this.strings.format(EXPECTED_RESPONSE_TYPE),
        cmd.responseClass().getSimpleName()
      );
    }

    attributes.put(
      this.strings.format(RECEIVED_RESPONSE_TYPE),
      responseActual.getClass().getSimpleName()
    );

    return new AzClientException(
      this.strings.format(ERROR_UNEXPECTED_RESPONSE_TYPE),
      AzStandardErrorCodes.errorProtocol(),
      Map.copyOf(attributes),
      Optional.empty(),
      Optional.empty()
    );
  }

  @Override
  public HBReadType<AzAMessageType> receive(
    final Duration timeout)
    throws AzClientException, InterruptedException
  {
    Objects.requireNonNull(timeout, "timeout");

    if (this.isClosed()) {
      throw this.errorClosed();
    }

    final var r =
      this.inbox.poll(timeout.toNanos(), TimeUnit.NANOSECONDS);

    if (r == null) {
      return new HBReadNothing<>();
    }

    return new HBReadResponse<>(r.sent(), r.received());
  }

  @Override
  public void send(
    final AzAMessageType message)
    throws AzClientException, InterruptedException
  {
    if (this.isClosed()) {
      throw this.errorClosed();
    }

    try {
      this.inbox.put(
        switch (message) {
          case final AzACommandLogin m -> {
            yield this.sendMessage(
              message,
              this.loginURI,
              Optional.empty()
            );
          }
          default -> {
            yield this.sendMessage(
              message,
              this.commandURI,
              Optional.empty()
            );
          }
        }
      );
    } catch (final IOException | AzProtocolException e) {
      throw AzClientException.ofException(e);
    }
  }

  @Override
  public void sendAndForget(
    final AzAMessageType message)
    throws AzClientException, InterruptedException
  {
    if (this.isClosed()) {
      throw this.errorClosed();
    }

    try {
      final var targetURI =
        switch (message) {
          case final AzACommandLogin ignored -> this.loginURI;
          default -> this.commandURI;
        };

      final var data =
        this.messages.serialize(message);

      final var response =
        this.http.send(
          HttpRequest.newBuilder()
            .uri(targetURI)
            .POST(HttpRequest.BodyPublishers.ofByteArray(data))
            .build(),
          HttpResponse.BodyHandlers.discarding()
        );

      LOG.debug("Send: Status {}", Integer.valueOf(response.statusCode()));
    } catch (final IOException e) {
      throw AzClientException.ofException(e);
    }
  }

  @Override
  public AzAMessageType sendAndWait(
    final AzAMessageType message,
    final Duration timeout)
    throws AzClientException, InterruptedException
  {
    if (this.isClosed()) {
      throw this.errorClosed();
    }

    try {
      return (
        switch (message) {
          case final AzACommandLogin m -> {
            yield this.sendMessage(
              message,
              this.loginURI,
              Optional.of(timeout)
            );
          }
          default -> {
            yield this.sendMessage(
              message,
              this.commandURI,
              Optional.of(timeout)
            );
          }
        }
      ).received();
    } catch (final IOException | AzProtocolException e) {
      throw AzClientException.ofException(e);
    }
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
  public Path fileDownload(
    final AzAssetID fileID,
    final Path file,
    final Path fileTmp,
    final long size,
    final String hashAlgorithm,
    final String hashValue,
    final Consumer<AzClientTransferStatistics> statistics)
    throws AzClientException
  {
    Objects.requireNonNull(fileID, "fileID");
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(fileTmp, "fileTmp");
    Objects.requireNonNull(hashAlgorithm, "hashAlgorithm");
    Objects.requireNonNull(hashValue, "hashValue");
    Objects.requireNonNull(statistics, "statistics");

    throw new RuntimeException();
  }

  @Override
  public void fileUpload(
    final AzAssetID fileID,
    final Path file,
    final String contentType,
    final String description,
    final Consumer<AzClientTransferStatistics> statistics)
    throws AzClientException
  {
    Objects.requireNonNull(fileID, "fileID");
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(contentType, "contentType");
    Objects.requireNonNull(description, "description");
    Objects.requireNonNull(statistics, "statistics");

    throw new RuntimeException();
  }

  @Override
  public List<AzAResponseType> transaction(
    final List<AzACommandType<?>> commands)
    throws InterruptedException, AzClientException
  {
    Objects.requireNonNull(commands, "commands");

    try (var out = new ByteArrayOutputStream();
         var dataOut = new DataOutputStream(out)) {

      for (final var command : commands) {
        final var data = this.messages.serialize(command);
        dataOut.writeInt(data.length);
        dataOut.write(data);
      }

      dataOut.writeInt(0);
      dataOut.flush();

      final var mainPublisher =
        HttpRequest.BodyPublishers.ofByteArray(out.toByteArray());

      final var request =
        HttpRequest.newBuilder(this.transactionURI)
          .POST(mainPublisher)
          .build();

      final var response =
        this.http.send(request, HttpResponse.BodyHandlers.ofByteArray());

      final var decompressed =
        decompressResponse(response, response.headers());

      final var results = new ArrayList<AzAResponseType>();
      try (var input = new ByteArrayInputStream(decompressed);
           var dataIn = new DataInputStream(input)) {
        while (true) {
          final var size =
            dataIn.readInt();

          if (size == 0) {
            return List.copyOf(results);
          }

          final var section =
            dataIn.readNBytes(size);
          final var message =
            this.messages.parse(section);

          results.add((AzAResponseType) message);
        }
      }
    } catch (final IOException | AzProtocolException e) {
      throw AzClientException.ofException(e);
    }
  }

  private void downloadFileAndHash(
    final AzTransferStatisticsTracker tracker,
    final InputStream stream,
    final Path file,
    final Path fileTmp,
    final String hashAlgorithm,
    final String hashValue)
    throws NoSuchAlgorithmException, IOException, AzClientException
  {
    final var options =
      new OpenOption[]{WRITE, TRUNCATE_EXISTING, CREATE};

    final var digest =
      MessageDigest.getInstance(hashAlgorithm);

    try (var output = Files.newOutputStream(fileTmp, options)) {
      try (var hashOut = new DigestOutputStream(output, digest)) {
        final var buffer = new byte[8192];

        while (true) {
          final var r = stream.read(buffer);
          if (r == -1) {
            break;
          }

          tracker.add(Integer.toUnsignedLong(r));
          hashOut.write(buffer, 0, r);
        }

        final var hashResult =
          HexFormat.of()
            .formatHex(digest.digest());

        if (!Objects.equals(hashResult, hashValue)) {
          throw AzClientException.ofError(
            new AzAResponseError(
              nullUUID(),
              this.strings.format(ERROR_HASH_VALUE_MISMATCH),
              errorIo(),
              Map.ofEntries(
                Map.entry(this.strings.format(EXPECTED_HASH), hashValue),
                Map.entry(this.strings.format(RECEIVED_HASH), hashResult),
                Map.entry(this.strings.format(HASH_ALGORITHM), hashAlgorithm)
              ),
              Optional.empty(),
              Optional.empty(),
              BLAME_CLIENT,
              List.of()
            )
          );
        }

        tracker.completed();
        Files.move(fileTmp, file, REPLACE_EXISTING, ATOMIC_MOVE);
      }
    }
  }

  private static String hashOfFile(
    final Path file)
    throws Exception
  {
    final var digest =
      MessageDigest.getInstance("SHA-256");

    try (var nullOut = OutputStream.nullOutputStream()) {
      try (var outputStream = new DigestOutputStream(nullOut, digest)) {
        try (var inputStream = Files.newInputStream(file)) {
          inputStream.transferTo(outputStream);
          return HexFormat.of().formatHex(digest.digest());
        }
      }
    }
  }
}
