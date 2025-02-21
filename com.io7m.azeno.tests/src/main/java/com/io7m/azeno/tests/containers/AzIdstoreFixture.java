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
import com.io7m.ervilla.api.EVolumeMount;
import com.io7m.idstore.admin_client.IdAClients;
import com.io7m.idstore.admin_client.api.IdAClientConfiguration;
import com.io7m.idstore.admin_client.api.IdAClientConnectionParameters;
import com.io7m.idstore.model.IdEmail;
import com.io7m.idstore.model.IdName;
import com.io7m.idstore.model.IdPasswordAlgorithmPBKDF2HmacSHA256;
import com.io7m.idstore.model.IdRealName;
import com.io7m.idstore.model.IdUser;
import com.io7m.idstore.protocol.admin.IdACommandUserCreate;
import com.io7m.idstore.server.api.IdServerBrandingConfiguration;
import com.io7m.idstore.server.api.IdServerConfigurationFile;
import com.io7m.idstore.server.api.IdServerDatabaseConfiguration;
import com.io7m.idstore.server.api.IdServerDatabaseKind;
import com.io7m.idstore.server.api.IdServerHTTPConfiguration;
import com.io7m.idstore.server.api.IdServerHTTPServiceConfiguration;
import com.io7m.idstore.server.api.IdServerHistoryConfiguration;
import com.io7m.idstore.server.api.IdServerMailConfiguration;
import com.io7m.idstore.server.api.IdServerMailTransportSMTP;
import com.io7m.idstore.server.api.IdServerMaintenanceConfiguration;
import com.io7m.idstore.server.api.IdServerPasswordExpirationConfiguration;
import com.io7m.idstore.server.api.IdServerRateLimitConfiguration;
import com.io7m.idstore.server.api.IdServerSessionConfiguration;
import com.io7m.idstore.server.service.configuration.IdServerConfigurationSerializers;
import com.io7m.idstore.tls.IdTLSDisabled;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.Optional.empty;

public final class AzIdstoreFixture
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AzIdstoreFixture.class);

  public static final String PASSWORD = "12345678";

  private final EContainerType serverContainer;
  private final IdUser userWithAdmin;
  private final IdUser userWithLogin;
  private final IdUser userWithoutLogin;
  private final AzPostgresFixture postgres;
  private final int adminAPIPort;
  private final int userAPIPort;
  private final int userViewPort;

  private AzIdstoreFixture(
    final AzPostgresFixture inPostgres,
    final EContainerType inServerContainer,
    final IdUser inUserWithAdmin,
    final IdUser inUserWithLogin,
    final IdUser inUserWithoutLogin,
    final int inAdminAPIPort,
    final int inUserAPIPort,
    final int inUserViewPort)
  {
    this.postgres =
      Objects.requireNonNull(inPostgres, "postgres");
    this.serverContainer =
      Objects.requireNonNull(inServerContainer, "serverContainer");

    this.userWithAdmin =
      Objects.requireNonNull(inUserWithAdmin, "inUserWithAdmin");
    this.userWithLogin =
      Objects.requireNonNull(inUserWithLogin, "user");
    this.userWithoutLogin =
      Objects.requireNonNull(inUserWithoutLogin, "inUserWithoutLogin");

    this.adminAPIPort = inAdminAPIPort;
    this.userAPIPort = inUserAPIPort;
    this.userViewPort = inUserViewPort;
  }

  public String password()
  {
    return PASSWORD;
  }

  public static AzIdstoreFixture create(
    final EContainerFactoryType supervisor,
    final AzPostgresFixture postgres,
    final Path baseDirectory,
    final int adminAPIPort,
    final int userAPIPort,
    final int userViewPort)
    throws Exception
  {
    final var adminName =
      "admin";

    final var users =
      Map.ofEntries(
        Map.entry("someone", UUID.randomUUID()),
        Map.entry("someone-nologin", UUID.randomUUID()),
        Map.entry("someone-admin", UUID.randomUUID())
      );

    LOG.info("Creating idstore");
    LOG.info("  Admin API:      {}:{}", "[::]", adminAPIPort);
    LOG.info("  User API:       {}:{}", "[::]", userAPIPort);
    LOG.info("  User View:      {}:{}", "[::]", userViewPort);
    LOG.info("  Admin name:     {}", adminName);
    LOG.info("  Admin password: {}", PASSWORD);

    for (final var user : users.entrySet()) {
      LOG.info("  User name:      {}", user.getKey());
      LOG.info("  User ID:        {}", user.getValue());
    }

    final var idstoreConfiguration =
      createServerConfiguration(
        postgres,
        adminAPIPort,
        userAPIPort,
        userViewPort
      );

    final var idstoreDirectory =
      baseDirectory.resolve("idstore");

    Files.createDirectories(idstoreDirectory);

    new IdServerConfigurationSerializers()
      .serializeFile(
        idstoreDirectory.resolve("server.xml"),
        idstoreConfiguration
      );

    {
      final var r =
        postgres.container()
          .executeAndWait(
            List.of(
              "dropdb",
              "--if-exists",
              "-f",
              "-w",
              "-U",
              postgres.databaseOwner(),
              "idstore"
            ),
            10L,
            TimeUnit.SECONDS
          );
    }

    final var r =
      postgres.container()
        .executeAndWait(
          List.of(
            "createdb",
            "-w",
            "-U",
            postgres.databaseOwner(),
            "idstore"
          ),
          10L,
          TimeUnit.SECONDS
        );

    Assertions.assertEquals(0, r, "Database creation succeeded");

    final var serverContainer =
      supervisor.start(
        EContainerSpec.builder(
            "quay.io",
            "io7mcom/idstore",
            AzTestProperties.IDSTORE_VERSION)
          .addVolumeMount(
            new EVolumeMount(idstoreDirectory, "/idstore/etc")
          )
          .addArgument("server")
          .addArgument("--verbose")
          .addArgument("debug")
          .addArgument("--configuration")
          .addArgument("/idstore/etc/server.xml")
          .setReadyCheck(new AzIdstoreReadyCheck(
            new EPortAddressType.All(),
            adminAPIPort))
          .build()
      );

    initialAdmin(serverContainer, UUID.randomUUID());

    final var userWithAdmin =
      createUser(
        adminName,
        PASSWORD,
        adminAPIPort,
        users.get("someone-admin"),
        "someone-admin"
      );

    final var userWithLogin =
      createUser(
        adminName,
        PASSWORD,
        adminAPIPort,
        users.get("someone"),
        "someone"
      );

    final var userWithoutLogin =
      createUser(
        adminName,
        PASSWORD,
        adminAPIPort,
        users.get("someone-nologin"),
        "someone-nologin"
      );

    return new AzIdstoreFixture(
      postgres,
      serverContainer,
      userWithAdmin,
      userWithLogin,
      userWithoutLogin,
      adminAPIPort,
      userAPIPort,
      userViewPort
    );
  }

  private static void initialAdmin(
    final EContainerType serverContainer,
    final UUID adminId)
    throws Exception
  {
    for (int index = 0; index < 5; ++index) {
      final var r = serverContainer.executeAndWaitIndefinitely(
        List.of(
          "idstore",
          "initial-admin",
          "--configuration",
          "/idstore/etc/server.xml",
          "--admin-id",
          adminId.toString(),
          "--admin-username",
          "admin",
          "--admin-password",
          "12345678",
          "--admin-email",
          "admin@example.com",
          "--admin-realname",
          "admin"
        )
      );
      if (r == 0) {
        return;
      }
      Thread.sleep(250L);
    }

    throw new IllegalStateException(
      "Failed to create initial admin after several attempts."
    );
  }

  private static IdServerConfigurationFile createServerConfiguration(
    final AzPostgresFixture postgres,
    final int adminAPIPort,
    final int userAPIPort,
    final int userViewPort)
  {
    return new IdServerConfigurationFile(
      new IdServerBrandingConfiguration("idstore", empty(), empty(), empty()),
      new IdServerMailConfiguration(
        new IdServerMailTransportSMTP("localhost", 25),
        empty(),
        "sender@example.com",
        Duration.ofHours(1L)
      ),
      new IdServerHTTPConfiguration(
        new IdServerHTTPServiceConfiguration(
          "[::]",
          adminAPIPort,
          URI.create("http://[::]:" + adminAPIPort + "/"),
          IdTLSDisabled.TLS_DISABLED
        ),
        new IdServerHTTPServiceConfiguration(
          "[::]",
          userAPIPort,
          URI.create("http://[::]:" + userAPIPort + "/"),
          IdTLSDisabled.TLS_DISABLED
        ),
        new IdServerHTTPServiceConfiguration(
          "[::]",
          userViewPort,
          URI.create("http://[::]:" + userViewPort + "/"),
          IdTLSDisabled.TLS_DISABLED
        )
      ),
      new IdServerDatabaseConfiguration(
        IdServerDatabaseKind.POSTGRESQL,
        postgres.databaseOwner(),
        PASSWORD,
        PASSWORD,
        empty(),
        "[::]",
        postgres.port(),
        "idstore",
        true,
        true
      ),
      new IdServerHistoryConfiguration(1, 1),
      new IdServerSessionConfiguration(
        Duration.ofHours(1000L),
        Duration.ofHours(1000L)),
      new IdServerRateLimitConfiguration(
        Duration.ofSeconds(1L),
        Duration.ofSeconds(1L),
        Duration.ofSeconds(1L),
        Duration.ofSeconds(0L),
        Duration.ofSeconds(1L),
        Duration.ofSeconds(0L)
      ),
      new IdServerPasswordExpirationConfiguration(
        empty(),
        empty()
      ),
      new IdServerMaintenanceConfiguration(empty()),
      empty()
    );
  }

  private static IdUser createUser(
    final String adminName,
    final String adminPassword,
    final int adminAPIPort,
    final UUID userId,
    final String userName)
    throws Exception
  {
    final var clients = new IdAClients();
    try (var client =
           clients.create(
             new IdAClientConfiguration(
               Clock.systemUTC(),
               Locale.ROOT)
           )
    ) {

      final var address =
        "http://%s:%d/".formatted("[::]", Integer.valueOf(adminAPIPort));

      client.connectOrThrow(
        new IdAClientConnectionParameters(
          adminName,
          adminPassword,
          URI.create(
            address
          ),
          Map.of(),
          Duration.ofSeconds(30L),
          Duration.ofSeconds(30L)
        )
      );

      return client.sendAndWaitOrThrow(
        new IdACommandUserCreate(
          Optional.of(userId),
          new IdName(userName),
          new IdRealName(userName),
          new IdEmail("%s@example.com".formatted(userName)),
          IdPasswordAlgorithmPBKDF2HmacSHA256.create()
            .createHashed(PASSWORD)
        ),
        Duration.ofSeconds(30L)
      ).user();
    }
  }

  public int adminAPIPort()
  {
    return this.adminAPIPort;
  }

  public int userAPIPort()
  {
    return this.userAPIPort;
  }

  public int userViewPort()
  {
    return this.userViewPort;
  }

  public IdName userWithLoginName()
  {
    return this.userWithLogin.idName();
  }

  public UUID userWithLoginId()
  {
    return this.userWithLogin.id();
  }

  public IdUser userWithLogin()
  {
    return this.userWithLogin;
  }

  public UUID userWithoutLoginId()
  {
    return this.userWithoutLogin.id();
  }

  public IdUser userWithoutLogin()
  {
    return this.userWithoutLogin;
  }

  public IdName userWithoutLoginName()
  {
    return this.userWithoutLogin.idName();
  }

  public IdName userWithAdminName()
  {
    return this.userWithAdmin.idName();
  }

  public UUID userWithAdminId()
  {
    return this.userWithAdmin.id();
  }

  public IdUser userWithAdmin()
  {
    return this.userWithAdmin;
  }
}
