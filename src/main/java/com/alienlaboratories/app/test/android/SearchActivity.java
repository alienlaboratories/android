// Copyright (c) 2013 Alien Laboratories, Inc.

package com.alienlaboratories.app.test.android;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Search activity.
 */
public final class SearchActivity extends Activity {

  private static final String LOG = SearchActivity.class.getSimpleName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    handleIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      String query = intent.getStringExtra(SearchManager.QUERY);
      Log.d(LOG, "Query: " + query);
    }
  }
}
