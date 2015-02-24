// Copyright (c) 2013 Alien Laboratories, Inc.

package com.alienlaboratories.app.test.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.alienlaboratories.app.test.android.data.Proxy;
import com.alienlaboratories.net.NetProto;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Google Authentication activity.
 * http://developer.android.com/reference/android/accounts/AccountManager.html
 * http://stackoverflow.com/questions/1996686/authtoken-from-accountmanager-in-android-client-no-longer-working
 */
public final class AuthActivity extends Activity {

  private static final String LOG = AuthActivity.class.getSimpleName();

  // TODO(burdon): Inject.
  private final Proxy proxy = Proxy.getInstance();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();

    Account account = (Account) getIntent().getExtras().get("account");
    boolean reset = getIntent().getExtras().getBoolean("reset", false);
    AccountManager.get(getApplicationContext())
        .getAuthToken(account, Proxy.AUTH_TOKEN_TYPE, null, false, new GetAuthTokenCallback(reset), null);
  }

  /**
   * Get the auth token (forces login, or reuses cached token).
   */
  private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {

    private final boolean reset;
    GetAuthTokenCallback(boolean reset) {
      this.reset = reset;
    }

    @Override
    public void run(AccountManagerFuture<Bundle> result) {
      try {
        Bundle bundle = result.getResult();
        Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
        if (intent != null) {
          // User input required.
          startActivity(intent); // TODO(burdon): Get result.
        } else {
          // Use the token to get the cookie.
          String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

          // TODO(burdon): Option to invalidate.
          if (reset) {
            Log.d(LOG, "Invalidating: " + authToken);
            AccountManager.get(getApplicationContext()).invalidateAuthToken(Proxy.ACCOUNT_TYPE, authToken);
            finish();
          } else {
            new GetCookieTask().execute(authToken);
          }
        }
      } catch (Exception ex) {
        Log.e(LOG, "Getting auth token", ex);
      }
    }
  }

  /**
   * Gets the ACSID cookie.
   */
  private class GetCookieTask extends AsyncTask<String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... tokens) {
      try {
        // Don't follow redirects (since we're expecting that).
        HttpURLConnection.setFollowRedirects(false);

        // Login.
        URL url = new URL(String.format(Proxy.LOGIN_URL, tokens[0]));
//      Log.v(LOG, "Login: " + url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // TODO(burdon): SSL?
        if (connection.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP) {
          // Response should be a redirect (with the cookie).
          return false;
        }

        // Check the cookie.
        return (proxy.getAuthCookie() != null);
      } catch (Exception ex) {
        Log.e(LOG, "Getting cookie", ex);
      } finally {
        HttpURLConnection.setFollowRedirects(true);
      }

      return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
      if (success) {
        // Test authenticated call.
        proxy.doAsyncPost(NetProto.Request.newBuilder()
            .setHeader(NetProto.Request.Header.newBuilder())
            .build());
        Log.d(LOG, "OK");
      } else {
        Log.e(LOG, "Failed");
      }

      finish();
    }
  }
}
