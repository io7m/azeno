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
package com.io7m.azeno.error_codes;

/**
 * <p>The standard error codes.</p>
 * <p>Note: This file is generated from codes.txt and should not be hand-edited.</p>
 */
public final class AzStandardErrorCodes
{
  private AzStandardErrorCodes()
  {
  }

  private static final AzErrorCode ERROR_API_MISUSE =
    new AzErrorCode("error-api-misuse");

  /**
   * An API was used incorrectly.
   *
   * @return The error code
   */
  public static AzErrorCode errorApiMisuse()
  {
    return ERROR_API_MISUSE;
  }

  private static final AzErrorCode ERROR_AUTHENTICATION =
    new AzErrorCode("error-authentication");

  /**
   * Authentication failed.
   *
   * @return The error code
   */
  public static AzErrorCode errorAuthentication()
  {
    return ERROR_AUTHENTICATION;
  }

  private static final AzErrorCode ERROR_CYCLIC =
    new AzErrorCode("error-cyclic");

  /**
   * A cycle was introduced into a structure that is not supposed to be cyclic.
   *
   * @return The error code
   */
  public static AzErrorCode errorCyclic()
  {
    return ERROR_CYCLIC;
  }

  private static final AzErrorCode ERROR_DUPLICATE =
    new AzErrorCode("error-duplicate");

  /**
   * An object already exists.
   *
   * @return The error code
   */
  public static AzErrorCode errorDuplicate()
  {
    return ERROR_DUPLICATE;
  }

  private static final AzErrorCode ERROR_HTTP_METHOD =
    new AzErrorCode("error-http-method");

  /**
   * The wrong HTTP method was used.
   *
   * @return The error code
   */
  public static AzErrorCode errorHttpMethod()
  {
    return ERROR_HTTP_METHOD;
  }

  private static final AzErrorCode ERROR_IO =
    new AzErrorCode("error-io");

  /**
   * An internal I/O error.
   *
   * @return The error code
   */
  public static AzErrorCode errorIo()
  {
    return ERROR_IO;
  }

  private static final AzErrorCode ERROR_NONEXISTENT =
    new AzErrorCode("error-nonexistent");

  /**
   * A requested object was not found.
   *
   * @return The error code
   */
  public static AzErrorCode errorNonexistent()
  {
    return ERROR_NONEXISTENT;
  }

  private static final AzErrorCode ERROR_NOT_LOGGED_IN =
    new AzErrorCode("error-not-logged-in");

  /**
   * A user is trying to perform an operation without having logged in.
   *
   * @return The error code
   */
  public static AzErrorCode errorNotLoggedIn()
  {
    return ERROR_NOT_LOGGED_IN;
  }

  private static final AzErrorCode ERROR_NO_SUPPORTED_PROTOCOLS =
    new AzErrorCode("error-no-supported-protocols");

  /**
   * The client and server have no supported protocols in common.
   *
   * @return The error code
   */
  public static AzErrorCode errorNoSupportedProtocols()
  {
    return ERROR_NO_SUPPORTED_PROTOCOLS;
  }

  private static final AzErrorCode ERROR_OPERATION_NOT_PERMITTED =
    new AzErrorCode("error-operation-not-permitted");

  /**
   * A generic "operation not permitted" error.
   *
   * @return The error code
   */
  public static AzErrorCode errorOperationNotPermitted()
  {
    return ERROR_OPERATION_NOT_PERMITTED;
  }

  private static final AzErrorCode ERROR_PARSE =
    new AzErrorCode("error-parse");

  /**
   * A parse error was encountered.
   *
   * @return The error code
   */
  public static AzErrorCode errorParse()
  {
    return ERROR_PARSE;
  }

  private static final AzErrorCode ERROR_PROTOCOL =
    new AzErrorCode("error-protocol");

  /**
   * A client sent a broken message of some kind.
   *
   * @return The error code
   */
  public static AzErrorCode errorProtocol()
  {
    return ERROR_PROTOCOL;
  }

  private static final AzErrorCode ERROR_REMOVE_IDENTIFIED_ITEMS =
    new AzErrorCode("error-remove-identified-items");

  /**
   * The items to be removed have serial numbers and cannot be removed as part of a set.
   *
   * @return The error code
   */
  public static AzErrorCode errorRemoveIdentifiedItems()
  {
    return ERROR_REMOVE_IDENTIFIED_ITEMS;
  }

  private static final AzErrorCode ERROR_REMOVE_TOO_MANY_ITEMS =
    new AzErrorCode("error-remove-too-many-items");

  /**
   * An attempt was made to remove more items than actually exist.
   *
   * @return The error code
   */
  public static AzErrorCode errorRemoveTooManyItems()
  {
    return ERROR_REMOVE_TOO_MANY_ITEMS;
  }

  private static final AzErrorCode ERROR_RESOURCE_CLOSE_FAILED =
    new AzErrorCode("error-resource-close-failed");

  /**
   * One or more resources failed to close.
   *
   * @return The error code
   */
  public static AzErrorCode errorResourceCloseFailed()
  {
    return ERROR_RESOURCE_CLOSE_FAILED;
  }

  private static final AzErrorCode ERROR_SECURITY_POLICY_DENIED =
    new AzErrorCode("error-security-policy-denied");

  /**
   * An operation was denied by the security policy.
   *
   * @return The error code
   */
  public static AzErrorCode errorSecurityPolicyDenied()
  {
    return ERROR_SECURITY_POLICY_DENIED;
  }

  private static final AzErrorCode ERROR_SQL_FOREIGN_KEY =
    new AzErrorCode("error-sql-foreign-key");

  /**
   * A violation of an SQL foreign key integrity constraint.
   *
   * @return The error code
   */
  public static AzErrorCode errorSqlForeignKey()
  {
    return ERROR_SQL_FOREIGN_KEY;
  }

  private static final AzErrorCode ERROR_SQL_REVISION =
    new AzErrorCode("error-sql-revision");

  /**
   * An internal SQL database error relating to database revisioning.
   *
   * @return The error code
   */
  public static AzErrorCode errorSqlRevision()
  {
    return ERROR_SQL_REVISION;
  }

  private static final AzErrorCode ERROR_SQL_UNIQUE =
    new AzErrorCode("error-sql-unique");

  /**
   * A violation of an SQL uniqueness constraint.
   *
   * @return The error code
   */
  public static AzErrorCode errorSqlUnique()
  {
    return ERROR_SQL_UNIQUE;
  }

  private static final AzErrorCode ERROR_SQL_UNSUPPORTED_QUERY_CLASS =
    new AzErrorCode("error-sql-unsupported-query-class");

  /**
   * An attempt was made to use a query class that is unsupported.
   *
   * @return The error code
   */
  public static AzErrorCode errorSqlUnsupportedQueryClass()
  {
    return ERROR_SQL_UNSUPPORTED_QUERY_CLASS;
  }

  private static final AzErrorCode ERROR_SQL =
    new AzErrorCode("error-sql");

  /**
   * An internal SQL database error.
   *
   * @return The error code
   */
  public static AzErrorCode errorSql()
  {
    return ERROR_SQL;
  }

  private static final AzErrorCode ERROR_TRASCO =
    new AzErrorCode("error-trasco");

  /**
   * An error raised by the Trasco database versioning library.
   *
   * @return The error code
   */
  public static AzErrorCode errorTrasco()
  {
    return ERROR_TRASCO;
  }

  private static final AzErrorCode ERROR_TYPE_CHECK_FAILED =
    new AzErrorCode("error-type-check-failed");

  /**
   * Type checking failed.
   *
   * @return The error code
   */
  public static AzErrorCode errorTypeCheckFailed()
  {
    return ERROR_TYPE_CHECK_FAILED;
  }

  private static final AzErrorCode ERROR_TYPE_CHECK_FIELD_INVALID =
    new AzErrorCode("error-type-field-invalid");

  /**
   * A field value did not match the provided pattern.
   *
   * @return The error code
   */
  public static AzErrorCode errorTypeCheckFieldInvalid()
  {
    return ERROR_TYPE_CHECK_FIELD_INVALID;
  }

  private static final AzErrorCode ERROR_TYPE_CHECK_FIELD_PATTERN_FAILURE =
    new AzErrorCode("error-type-field-pattern-invalid");

  /**
   * A field pattern was invalid.
   *
   * @return The error code
   */
  public static AzErrorCode errorTypeCheckFieldPatternFailure()
  {
    return ERROR_TYPE_CHECK_FIELD_PATTERN_FAILURE;
  }

  private static final AzErrorCode ERROR_TYPE_CHECK_FIELD_REQUIRED_MISSING =
    new AzErrorCode("error-type-field-required-missing");

  /**
   * A field was required but is missing.
   *
   * @return The error code
   */
  public static AzErrorCode errorTypeCheckFieldRequiredMissing()
  {
    return ERROR_TYPE_CHECK_FIELD_REQUIRED_MISSING;
  }

  private static final AzErrorCode ERROR_TYPE_FIELD_TYPE_NONEXISTENT =
    new AzErrorCode("error-type-field-type-nonexistent");

  /**
   * A field in the type declaration refers to a nonexistent type.
   *
   * @return The error code
   */
  public static AzErrorCode errorTypeFieldTypeNonexistent()
  {
    return ERROR_TYPE_FIELD_TYPE_NONEXISTENT;
  }

  private static final AzErrorCode ERROR_TYPE_REFERENCED =
    new AzErrorCode("error-type-referenced");

  /**
   * The type is referenced by one or more existing items.
   *
   * @return The error code
   */
  public static AzErrorCode errorTypeReferenced()
  {
    return ERROR_TYPE_REFERENCED;
  }

  private static final AzErrorCode ERROR_TYPE_SCALAR_REFERENCED =
    new AzErrorCode("error-type-scalar-referenced");

  /**
   * The scalar type is referenced by one or more existing types/fields.
   *
   * @return The error code
   */
  public static AzErrorCode errorTypeScalarReferenced()
  {
    return ERROR_TYPE_SCALAR_REFERENCED;
  }

  private static final AzErrorCode ERROR_USER_NONEXISTENT =
    new AzErrorCode("error-user-nonexistent");

  /**
   * An attempt was made to reference a user that does not exist.
   *
   * @return The error code
   */
  public static AzErrorCode errorUserNonexistent()
  {
    return ERROR_USER_NONEXISTENT;
  }

  private static final AzErrorCode ERROR_ITEM_STILL_IN_LOCATION =
    new AzErrorCode("error-item-still-in-location");

  /**
   * An item cannot be deleted when instances of it are still present in one or more locations.
   *
   * @return The error code
   */
  public static AzErrorCode errorItemStillInLocation()
  {
    return ERROR_ITEM_STILL_IN_LOCATION;
  }

  private static final AzErrorCode ERROR_LOCATION_NOT_EMPTY =
    new AzErrorCode("error-location-not-empty");

  /**
   * A location cannot be deleted while it still contains one or more items.
   *
   * @return The error code
   */
  public static AzErrorCode errorLocationNotEmpty()
  {
    return ERROR_LOCATION_NOT_EMPTY;
  }

  private static final AzErrorCode ERROR_LOCATION_NON_DELETED_CHILDREN =
    new AzErrorCode("error-location-non-deleted-children");

  /**
   * A location cannot be deleted while it still has non-deleted child locations.
   *
   * @return The error code
   */
  public static AzErrorCode errorLocationNonDeletedChildren()
  {
    return ERROR_LOCATION_NON_DELETED_CHILDREN;
  }
}

