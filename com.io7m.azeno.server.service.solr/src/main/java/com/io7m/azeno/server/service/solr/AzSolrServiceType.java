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


package com.io7m.azeno.server.service.solr;

import com.io7m.azeno.error_codes.AzException;
import com.io7m.azeno.model.AzAsset;
import com.io7m.azeno.model.AzAssetSummary;
import com.io7m.azeno.model.AzCollection;
import com.io7m.azeno.model.AzCollectionID;
import com.io7m.azeno.model.AzSchema;
import com.io7m.repetoir.core.RPServiceType;

import java.util.List;
import java.util.Objects;

/**
 * The Solr service.
 */

public interface AzSolrServiceType
  extends RPServiceType, AutoCloseable
{
  /**
   * Create a collection.
   *
   * @param collection The collection
   * @param schema     The collection schema
   *
   * @throws AzException On errors
   */

  void collectionCreate(
    AzCollection collection,
    AzSchema schema)
    throws AzException;

  /**
   * Index an asset.
   *
   * @param asset The asset
   *
   * @throws AzException On errors
   */

  default void assetIndex(
    final AzAsset asset)
    throws AzException
  {
    Objects.requireNonNull(asset, "asset");
    this.assetsIndex(List.of(asset));
  }

  /**
   * Index a list of assets.
   *
   * @param assets The assets
   *
   * @throws AzException On errors
   */

  void assetsIndex(
    List<AzAsset> assets)
    throws AzException;

  /**
   * Start searching for assets.
   *
   * @param collection The collection
   * @param queryText  The query text
   * @param sortField  The sort field
   *
   * @return A page of results
   *
   * @throws AzException On errors
   */

  AzSolrPage<AzAssetSummary> assetSearchBegin(
    AzCollectionID collection,
    String queryText,
    AzSolrSortField sortField)
    throws AzException;

  @Override
  void close()
    throws AzException;
}
