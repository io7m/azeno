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

package com.io7m.azeno.tests.server.controller;


import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.server.controller.asset.AzACommandContext;
import com.io7m.azeno.server.service.clock.AzServerClock;
import com.io7m.azeno.server.service.sessions.AzSession;
import com.io7m.azeno.server.service.sessions.AzSessionSecretIdentifier;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryNoOp;
import com.io7m.azeno.server.service.telemetry.api.AzServerTelemetryServiceType;
import com.io7m.azeno.strings.AzStrings;
import com.io7m.azeno.tests.AzFakeClock;
import com.io7m.idstore.model.IdName;
import com.io7m.medrina.api.MRoleName;
import com.io7m.medrina.api.MSubject;
import com.io7m.repetoir.core.RPServiceDirectory;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public abstract class AzCmdAbstractContract
{
  private RPServiceDirectory services;
  private AzDatabaseTransactionType transaction;
  private AzFakeClock clock;
  private AzServerClock serverClock;
  private AzStrings strings;
  private OffsetDateTime timeStart;
  private AzUser user;
  private AzACommandContext context;

  protected final OffsetDateTime timeStart()
  {
    return this.timeStart;
  }

  @BeforeEach
  protected final void commandSetup()
    throws Exception
  {
    this.services =
      new RPServiceDirectory();
    this.transaction =
      Mockito.mock(AzDatabaseTransactionType.class);

    this.clock =
      new AzFakeClock();
    this.serverClock =
      new AzServerClock(this.clock);
    this.timeStart =
      this.serverClock.now();
    this.strings =
      AzStrings.create(Locale.ROOT);

    this.user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    this.services.register(
      AzServerClock.class,
      this.serverClock);
    this.services.register(
      AzStrings.class,
      this.strings);
    this.services.register(
      AzServerTelemetryServiceType.class,
      AzServerTelemetryNoOp.noop());
  }

  protected final void setRoles(
    final MRoleName... roles)
  {
    this.user = new AzUser(
      this.user.userId(),
      new IdName("x"),
      new MSubject(Set.of(roles))
    );
  }

  @AfterEach
  protected final void commandTearDown()
    throws Exception
  {
    this.services.close();
  }

  protected final RPServiceDirectoryType services()
  {
    return this.services;
  }

  protected final AzDatabaseTransactionType transaction()
  {
    return this.transaction;
  }

  protected final AzACommandContext createContext()
  {
    final var session =
      new AzSession(AzSessionSecretIdentifier.generate(), this.user);

    return new AzACommandContext(
      this.services,
      UUID.randomUUID(),
      this.transaction,
      session,
      "127.0.0.1",
      "Tests"
    );
  }
}
