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


package com.io7m.azeno.client_database.api;

import com.io7m.azeno.strings.AzStrings;
import com.io7m.darco.api.DDatabaseCreate;
import com.io7m.darco.api.DDatabaseTelemetryNoOp;
import com.io7m.darco.api.DDatabaseTelemetryType;
import com.io7m.darco.api.DDatabaseUpgrade;
import com.io7m.darco.sqlite.DSDatabaseConfigurationType;

import java.nio.file.Path;
import java.util.Objects;

/**
 * The configuration information for the SQLite database.
 *
 * @param strings The string resources
 * @param create  The database creation option
 * @param upgrade The database upgrade option
 * @param file    The database file
 */

public record AzCDatabaseConfiguration(
  AzStrings strings,
  DDatabaseCreate create,
  DDatabaseUpgrade upgrade,
  Path file)
  implements DSDatabaseConfigurationType
{
  /**
   * The configuration information for the SQLite database.
   *
   * @param strings The string resources
   * @param create  The database creation option
   * @param upgrade The database upgrade option
   * @param file    The database file
   */

  public AzCDatabaseConfiguration
  {
    Objects.requireNonNull(strings, "strings");
    Objects.requireNonNull(create, "create");
    Objects.requireNonNull(upgrade, "upgrade");
    Objects.requireNonNull(file, "file");
  }

  @Override
  public DDatabaseTelemetryType telemetry()
  {
    return DDatabaseTelemetryNoOp.get();
  }
}
