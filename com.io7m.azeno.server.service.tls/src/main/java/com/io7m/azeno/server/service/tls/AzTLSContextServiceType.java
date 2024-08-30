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


package com.io7m.azeno.server.service.tls;


import com.io7m.azeno.error_codes.AzException;
import com.io7m.azeno.tls.AzTLSContext;
import com.io7m.azeno.tls.AzTLSStoreConfiguration;
import com.io7m.repetoir.core.RPServiceType;

/**
 * A service that provides preconfigured TLS contexts.
 */

public interface AzTLSContextServiceType
  extends RPServiceType
{
  /**
   * Create a TLS context.
   *
   * @param user                    The user of this context (such as "HealthService", "AdminService", etc)
   * @param keyStoreConfiguration   The keystore configuration
   * @param trustStoreConfiguration The truststore configuration
   *
   * @return A TLS context
   *
   * @throws AzException On errors
   */

  AzTLSContext create(
    String user,
    AzTLSStoreConfiguration keyStoreConfiguration,
    AzTLSStoreConfiguration trustStoreConfiguration)
    throws AzException;

  /**
   * Reload all TLS contexts. Primarily used to reload short-lived certificates
   * issued using the ACME protocol.
   */

  void reload();
}
