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

package com.io7m.azeno.server.api;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration information for OpenTelemetry.
 *
 * @param logicalServiceName The logical service name
 * @param logs               The configuration for OTLP logs
 * @param metrics            The configuration for OTLP metrics
 * @param traces             The configuration for OTLP traces
 */

public record AzServerOpenTelemetryConfiguration(
  String logicalServiceName,
  Optional<AzLogs> logs,
  Optional<AzMetrics> metrics,
  Optional<AzTraces> traces)
{
  /**
   * Configuration information for OpenTelemetry.
   *
   * @param logicalServiceName The logical service name
   * @param logs               The configuration for OTLP logs
   * @param metrics            The configuration for OTLP metrics
   * @param traces             The configuration for OTLP traces
   */

  public AzServerOpenTelemetryConfiguration
  {
    Objects.requireNonNull(logicalServiceName, "logicalServiceName");
    Objects.requireNonNull(logs, "logs");
    Objects.requireNonNull(metrics, "metrics");
    Objects.requireNonNull(traces, "traces");
  }

  /**
   * The protocol used to deliver OpenTelemetry data.
   */

  public enum AzOTLPProtocol
  {
    /**
     * gRPC
     */

    GRPC,

    /**
     * HTTP(s)
     */

    HTTP
  }

  /**
   * Metrics configuration.
   *
   * @param endpoint The endpoint to which OTLP metrics data will be sent.
   * @param protocol The protocol used to deliver OpenTelemetry data.
   */

  public record AzMetrics(
    URI endpoint,
    AzOTLPProtocol protocol)
  {
    /**
     * Metrics configuration.
     */

    public AzMetrics
    {
      Objects.requireNonNull(endpoint, "endpoint");
      Objects.requireNonNull(protocol, "protocol");
    }
  }

  /**
   * Trace configuration.
   *
   * @param endpoint The endpoint to which OTLP trace data will be sent.
   * @param protocol The protocol used to deliver OpenTelemetry data.
   */

  public record AzTraces(
    URI endpoint,
    AzOTLPProtocol protocol)
  {
    /**
     * Trace configuration.
     */

    public AzTraces
    {
      Objects.requireNonNull(endpoint, "endpoint");
      Objects.requireNonNull(protocol, "protocol");
    }
  }

  /**
   * Logs configuration.
   *
   * @param endpoint The endpoint to which OTLP log data will be sent.
   * @param protocol The protocol used to deliver OpenTelemetry data.
   */

  public record AzLogs(
    URI endpoint,
    AzOTLPProtocol protocol)
  {
    /**
     * Logs configuration.
     */

    public AzLogs
    {
      Objects.requireNonNull(endpoint, "endpoint");
      Objects.requireNonNull(protocol, "protocol");
    }
  }
}
