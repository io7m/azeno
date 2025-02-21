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


package com.io7m.azeno.tests;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public final class AzFakeClock extends Clock
{
  private long time;

  public AzFakeClock()
  {
    this.time = 0L;
  }

  @Override
  public ZoneId getZone()
  {
    return ZoneId.of("UTC");
  }

  @Override
  public Clock withZone(
    final ZoneId zone)
  {
    return this;
  }

  @Override
  public Instant instant()
  {
    ++this.time;
    return Instant.ofEpochSecond(this.time);
  }

  public void setTime(
    final Instant newTime)
  {
    this.time = newTime.getEpochSecond();
  }
}
