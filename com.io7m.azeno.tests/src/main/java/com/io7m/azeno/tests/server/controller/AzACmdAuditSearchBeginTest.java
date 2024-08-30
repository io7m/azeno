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

import com.io7m.azeno.database.api.AzAuditSearchType;
import com.io7m.azeno.database.api.AzDatabaseAuditSearchType;
import com.io7m.azeno.model.AzAuditEvent;
import com.io7m.azeno.model.AzAuditSearchParameters;
import com.io7m.azeno.model.AzComparisonExactType;
import com.io7m.azeno.model.AzPage;
import com.io7m.azeno.model.AzTimeRange;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.protocol.asset.AzACommandAuditSearchBegin;
import com.io7m.azeno.security.AzSecurity;
import com.io7m.azeno.server.controller.asset.AzACmdAuditSearchBegin;
import com.io7m.azeno.server.controller.command_exec.AzCommandExecutionFailure;
import com.io7m.medrina.api.MMatchActionType.MMatchActionWithName;
import com.io7m.medrina.api.MMatchObjectType.MMatchObjectWithType;
import com.io7m.medrina.api.MMatchSubjectType.MMatchSubjectWithRolesAny;
import com.io7m.medrina.api.MPolicy;
import com.io7m.medrina.api.MRule;
import com.io7m.medrina.api.MRuleName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorSecurityPolicyDenied;
import static com.io7m.azeno.security.AzSecurityPolicy.AUDIT;
import static com.io7m.azeno.security.AzSecurityPolicy.READ;
import static com.io7m.azeno.security.AzSecurityPolicy.ROLE_AUDIT_READER;
import static com.io7m.medrina.api.MRuleConclusion.ALLOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @see AzACmdAuditSearchBegin
 */

public final class AzACmdAuditSearchBeginTest
  extends AzCmdAbstractContract
{
  private static final AzAuditSearchParameters PARAMETERS =
    new AzAuditSearchParameters(
      Optional.empty(),
      new AzComparisonExactType.Anything<>(),
      new AzTimeRange(
        OffsetDateTime.now().minusDays(365L),
        OffsetDateTime.now().plusDays(356L)
      ),
      100L
    );

  /**
   * Searching for events requires the permission to READ to AUDIT.
   *
   * @throws Exception On errors
   */

  @Test
  public void testNotAllowed0()
    throws Exception
  {
    /* Arrange. */

    final var context =
      this.createContext();

    /* Act. */

    final var handler =
      new AzACmdAuditSearchBegin();
    final var ex =
      assertThrows(AzCommandExecutionFailure.class, () -> {
        handler.execute(
          context,
          new AzACommandAuditSearchBegin(PARAMETERS));
      });

    /* Assert. */

    assertEquals(errorSecurityPolicyDenied(), ex.errorCode());
  }

  /**
   * Searching for audit events works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSearch()
    throws Exception
  {
    /* Arrange. */

    final var events =
      mock(AzAuditSearchType.class);
    final var fileSearch =
      mock(AzDatabaseAuditSearchType.class);

    final var transaction =
      this.transaction();

    final var pageMain =
      new AzPage<>(
        List.of(
          new AzAuditEvent(
            0L,
            OffsetDateTime.now().withNano(0).plusSeconds(1L),
            AzUserID.random(),
            "T",
            Map.of()
          ),
          new AzAuditEvent(
            1L,
            OffsetDateTime.now().withNano(0).plusSeconds(2L),
            AzUserID.random(),
            "U",
            Map.of()
          ),
          new AzAuditEvent(
            2L,
            OffsetDateTime.now().withNano(0).plusSeconds(3L),
            AzUserID.random(),
            "V",
            Map.of()
          )
        ),
        1,
        1,
        0L
      );

    when(transaction.query(AzAuditSearchType.class))
      .thenReturn(events);
    when(events.execute(any()))
      .thenReturn(fileSearch);
    when(fileSearch.pageCurrent(any()))
      .thenReturn(pageMain);

    AzSecurity.setPolicy(new MPolicy(List.of(
      new MRule(
        MRuleName.of("rule0"),
        "",
        ALLOW,
        new MMatchSubjectWithRolesAny(Set.of(ROLE_AUDIT_READER)),
        new MMatchObjectWithType(AUDIT.type()),
        new MMatchActionWithName(READ)
      )
    )));

    this.setRoles(ROLE_AUDIT_READER);

    final var context =
      this.createContext();

    /* Act. */

    final var handler = new AzACmdAuditSearchBegin();

    handler.execute(context, new AzACommandAuditSearchBegin(PARAMETERS));

    /* Assert. */

    verify(transaction)
      .query(AzAuditSearchType.class);
    verify(events)
      .execute(PARAMETERS);
    verify(fileSearch)
      .pageCurrent(transaction);

    verifyNoMoreInteractions(fileSearch);
    verifyNoMoreInteractions(transaction);
    verifyNoMoreInteractions(events);
  }
}
