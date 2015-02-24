// Copyright (c) 2013 Alien Laboratories, Inc.

package com.alienlaboratories.app.test.android;

import android.app.Activity;
import android.os.Bundle;

/**
 * Settings activity.
 * http://developer.android.com/guide/topics/ui/settings.html
 */
public final class SettingsActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Display the fragment as the main content.
    getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new SettingsFragment())
        .commit();
  }
}
