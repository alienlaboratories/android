// Copyright (c) 2013 Alien Laboratories, Inc.

package com.alienlaboratories.app.test.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.alienlaboratories.app.test.android.data.Proxy;

/**
 * Settings fragment.
 */
public final class SettingsFragment extends PreferenceFragment {

  private static final String LOG = SettingsFragment.class.getSimpleName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);

    // Set accounts.
    // TODO(burdon): Wrap accounts class.
    // http://blog.notdot.net/2010/05/Authenticating-against-App-Engine-from-an-Android-app
    final Account[] accounts =
        AccountManager.get(getActivity().getApplicationContext()).getAccountsByType(Proxy.ACCOUNT_TYPE);

    int i = 0;
    CharSequence[] entries = new CharSequence[accounts.length];
    CharSequence[] values = new CharSequence[accounts.length];
    for (Account account : accounts) {
      entries[i] = account.name;
      values[i] = Integer.toString(i);
      i++;
    }

    // Select account.
    final ListPreference prefAccount = (ListPreference) findPreference("pref_account");
    prefAccount.setSummary(prefAccount.getEntry());
    prefAccount.setEntries(entries);
    prefAccount.setEntryValues(values);

    prefAccount.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference.getKey().equals("pref_account")) {
          prefAccount.setSummary(prefAccount.getEntry());

          Account account = accounts[Integer.parseInt((String) value)];
          startActivity(new Intent(getActivity(), AuthActivity.class)
              .putExtra("account", account));
        }

        return true;
      }
    });

    // Reset.
    findPreference("pref_reset").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Account account = accounts[Integer.parseInt(prefAccount.getValue())];
        startActivity(new Intent(getActivity(), AuthActivity.class)
            .putExtra("account", account)
            .putExtra("reset", true));

        return true;
      }
    });

    // Test
    // TODO(burdon): Factor out tests.
    findPreference("pref_test").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Proxy.tests();
        return true;
      }
    });
  }
}
