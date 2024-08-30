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

package com.io7m.azeno.tests.database;

import com.io7m.azeno.database.api.AzDatabaseConnectionType;
import com.io7m.azeno.database.api.AzDatabaseTransactionType;
import com.io7m.azeno.database.api.AzDatabaseType;
import com.io7m.azeno.database.api.AzSchemaGetType;
import com.io7m.azeno.database.api.AzSchemaPutType;
import com.io7m.azeno.database.api.AzSchemaSearchType;
import com.io7m.azeno.model.AzSchema;
import com.io7m.azeno.model.AzSchemaFieldBoolean;
import com.io7m.azeno.model.AzSchemaFieldFloating;
import com.io7m.azeno.model.AzSchemaFieldInteger;
import com.io7m.azeno.model.AzSchemaFieldStringLocal;
import com.io7m.azeno.model.AzSchemaFieldStringUninterpreted;
import com.io7m.azeno.model.AzSchemaFieldTimestamp;
import com.io7m.azeno.model.AzSchemaFieldType;
import com.io7m.azeno.model.AzSchemaFieldURI;
import com.io7m.azeno.model.AzSchemaFieldUUID;
import com.io7m.azeno.model.AzSchemaID;
import com.io7m.azeno.model.AzUnit;
import com.io7m.azeno.model.AzUser;
import com.io7m.azeno.model.AzUserID;
import com.io7m.azeno.tests.containers.AzDatabaseFixture;
import com.io7m.azeno.tests.containers.AzFixtures;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.ervilla.api.EContainerSupervisorType;
import com.io7m.ervilla.test_extension.ErvillaCloseAfterSuite;
import com.io7m.ervilla.test_extension.ErvillaConfiguration;
import com.io7m.ervilla.test_extension.ErvillaExtension;
import com.io7m.idstore.model.IdName;
import com.io7m.lanark.core.RDottedName;
import com.io7m.medrina.api.MSubject;
import com.io7m.zelador.test_extension.CloseableResourcesType;
import com.io7m.zelador.test_extension.ZeladorExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.io7m.azeno.error_codes.AzStandardErrorCodes.errorDuplicate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({ErvillaExtension.class, ZeladorExtension.class})
@ErvillaConfiguration(projectName = "com.io7m.azeno", disabledIfUnsupported = true)
public final class AzDatabaseSchemasTest
{
  private static AzDatabaseFixture DATABASE_FIXTURE;
  private AzDatabaseConnectionType connection;
  private AzDatabaseTransactionType transaction;
  private AzDatabaseType database;

  private AzUser user;
  private AzSchemaPutType schemaPut;
  private AzSchemaGetType schemaGet;
  private AzSchemaSearchType schemaSearch;
  private AzSchema schema1;
  private AzSchema schema2;
  private AzSchema schema3;

  @BeforeAll
  public static void setupOnce(
    final @ErvillaCloseAfterSuite EContainerSupervisorType containers)
    throws Exception
  {
    DATABASE_FIXTURE =
      AzFixtures.database(AzFixtures.pod(containers));
  }

  @BeforeEach
  public void setup(
    final CloseableResourcesType closeables)
    throws Exception
  {
    DATABASE_FIXTURE.reset();

    this.database =
      closeables.addPerTestResource(DATABASE_FIXTURE.createDatabase());
    this.connection =
      closeables.addPerTestResource(this.database.openConnection());
    this.transaction =
      closeables.addPerTestResource(this.connection.openTransaction());

    this.schemaPut =
      this.transaction.query(AzSchemaPutType.class);
    this.schemaGet =
      this.transaction.query(AzSchemaGetType.class);
    this.schemaSearch =
      this.transaction.query(AzSchemaSearchType.class);

    this.user =
      new AzUser(
        AzUserID.random(),
        new IdName("x"),
        new MSubject(Set.of())
      );

    this.schema1 =
      new AzSchema(
        new AzSchemaID(
          new RDottedName("com.io7m.example"),
          1
        ),
        Stream.of(
          new AzSchemaFieldBoolean(new RDottedName("a.k0"), true),
          new AzSchemaFieldFloating(new RDottedName("a.k1"), true),
          new AzSchemaFieldInteger(new RDottedName("a.k2"), true),
          new AzSchemaFieldStringLocal(new RDottedName("a.k6"), Locale.of("eng"), true),
          new AzSchemaFieldStringUninterpreted(new RDottedName("a.k8"), true),
          new AzSchemaFieldTimestamp(new RDottedName("a.k3"), true),
          new AzSchemaFieldURI(new RDottedName("a.k4"), true),
          new AzSchemaFieldUUID(new RDottedName("a.k5"), true)
        ).collect(Collectors.toMap(AzSchemaFieldType::name, f -> f))
      );

    this.schema2 =
      new AzSchema(
        new AzSchemaID(
          new RDottedName("com.io7m.example"),
          2
        ),
        this.schema1.fieldTypes()
      );

    this.schema3 =
      new AzSchema(
        new AzSchemaID(
          new RDottedName("com.io7m.example"),
          3
        ),
        this.schema1.fieldTypes()
      );
  }

  /**
   * Creating schemas works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSchemaCreate0()
    throws Exception
  {
    this.transaction.setUserID(this.user.userId());

    this.schemaPut.execute(this.schema1);
    assertEquals(
      this.schema1,
      this.schemaGet.execute(this.schema1.id()).orElseThrow()
    );
  }

  /**
   * Schemas cannot be updated.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSchemaCreate1()
    throws Exception
  {
    this.transaction.setUserID(this.user.userId());

    this.schemaPut.execute(this.schema1);

    final var ex =
      assertThrows(DDatabaseException.class, () -> {
      this.schemaPut.execute(this.schema1);
    });

    assertEquals(errorDuplicate().id(), ex.errorCode());
  }

  /**
   * Searching schemas works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSchemaSearch0()
    throws Exception
  {
    this.transaction.setUserID(this.user.userId());

    this.schemaPut.execute(this.schema1);
    this.schemaPut.execute(this.schema2);
    this.schemaPut.execute(this.schema3);

    assertEquals(
      List.of(
        this.schema1.id(),
        this.schema2.id(),
        this.schema3.id()
      ),
      this.schemaSearch.execute(AzUnit.UNIT)
        .pageCurrent(this.transaction)
        .items()
    );
  }
}
