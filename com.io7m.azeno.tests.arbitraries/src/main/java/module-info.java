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

import net.jqwik.api.providers.ArbitraryProvider;

/**
 * Digital asset server (Arbitrary instances)
 */

module com.io7m.azeno.tests.arbitraries
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires com.io7m.azeno.error_codes;
  requires com.io7m.azeno.model;
  requires com.io7m.azeno.protocol.asset;

  requires com.io7m.idstore.model;
  requires com.io7m.lanark.core;
  requires com.io7m.medrina.api;
  requires com.io7m.seltzer.api;
  requires com.io7m.verona.core;
  requires net.jqwik.api;

  exports com.io7m.azeno.tests.arbitraries;
  exports com.io7m.azeno.tests.arbitraries.model;

  uses ArbitraryProvider;

  provides ArbitraryProvider with
com.io7m.azeno.tests.arbitraries.model.AzArbDottedName,
com.io7m.azeno.tests.arbitraries.model.AzArbUserID,
com.io7m.azeno.tests.arbitraries.model.AzArbAuditSearchParameters,
com.io7m.azeno.tests.arbitraries.model.AzArbErrorCode,
com.io7m.azeno.tests.arbitraries.model.AzArbOffsetDateTime,
com.io7m.azeno.tests.arbitraries.model.AzArbAuditEvent,
com.io7m.azeno.tests.arbitraries.model.AzArbVersion,
com.io7m.azeno.tests.arbitraries.model.AzArbTimeRange,
com.io7m.azeno.tests.arbitraries.model.AzArbMRoleName,
com.io7m.azeno.tests.arbitraries.AzArbCommand,
com.io7m.azeno.tests.arbitraries.AzArbCommandAuditSearchBegin,
com.io7m.azeno.tests.arbitraries.AzArbCommandAuditSearchNext,
com.io7m.azeno.tests.arbitraries.AzArbCommandAuditSearchPrevious,
com.io7m.azeno.tests.arbitraries.AzArbCommandDebugInvalid,
com.io7m.azeno.tests.arbitraries.AzArbCommandDebugRandom,
com.io7m.azeno.tests.arbitraries.AzArbCommandLogin,
com.io7m.azeno.tests.arbitraries.AzArbCommandRolesAssign,
com.io7m.azeno.tests.arbitraries.AzArbCommandRolesGet,
com.io7m.azeno.tests.arbitraries.AzArbCommandRolesRevoke,
com.io7m.azeno.tests.arbitraries.AzArbResponse,
com.io7m.azeno.tests.arbitraries.AzArbResponseAuditSearch,
com.io7m.azeno.tests.arbitraries.AzArbResponseError,
com.io7m.azeno.tests.arbitraries.AzArbResponseLogin,
com.io7m.azeno.tests.arbitraries.AzArbResponseRolesGet
    ;
}
