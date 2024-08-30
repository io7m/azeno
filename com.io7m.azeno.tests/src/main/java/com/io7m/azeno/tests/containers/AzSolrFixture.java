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


package com.io7m.azeno.tests.containers;

import com.io7m.azeno.tests.AzTestProperties;
import com.io7m.ervilla.api.EContainerFactoryType;
import com.io7m.ervilla.api.EContainerSpec;
import com.io7m.ervilla.api.EContainerType;
import com.io7m.ervilla.api.EPortAddressType;
import com.io7m.ervilla.api.EPortProtocol;
import com.io7m.ervilla.api.EPortPublish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A Solr fixture.
 */

public final class AzSolrFixture
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AzIdstoreFixture.class);

  private final EContainerType container;
  private final int port;

  private AzSolrFixture(
    final EContainerType inContainer,
    final int inPort)
  {
    this.container =
      Objects.requireNonNull(inContainer, "container");
    this.port =
      inPort;
  }

  public static AzSolrFixture create(
    final EContainerFactoryType supervisor,
    final int port)
    throws Exception
  {
    LOG.info(
      "Creating solr instance on {}", Integer.valueOf(port));

    final var spec =
      EContainerSpec.builder("docker.io", "library/solr", AzTestProperties.SOLR_VERSION)
        .addPublishPort(
          new EPortPublish(
            new EPortAddressType.All(),
            port,
            8983,
            EPortProtocol.TCP
          )
        )
        .addArgument("-c")
        .addArgument("-f")
        .setReadyCheck(new AzSolrReadyCheck(new EPortAddressType.All(), port))
        .build();

    return new AzSolrFixture(
      supervisor.start(spec),
      port
    );
  }

  public EContainerType container()
  {
    return this.container;
  }

  public int port()
  {
    return this.port;
  }
}
