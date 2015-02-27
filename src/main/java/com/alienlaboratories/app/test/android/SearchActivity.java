// Copyright (c) 2013 Alien Laboratories, Inc.

package com.alienlaboratories.app.test.android;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

/**
 * Search activity.
 */
public final class SearchActivity extends Activity {

  private static final String LOG = SearchActivity.class.getSimpleName();

  /*
  private final Injector injector = Guice.createInjector(new TestTypeModule());
  private final Proxy proxy = injector.getInstance(Proxy.class);
  */

  // TODO(burdon): Abstract (so testable). Create mock. Move proxy into model.
//  private final Proxy proxy = Proxy.getInstance();

  // http://developer.android.com/guide/topics/search/search-dialog.html
  // http://developer.android.com/training/search/setup.html

  // NOTE: The action bar is included in all activities that use Theme.Holo (disable via Theme.Holo.NoActionBar)

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleIntent(getIntent());
  }

  // http://developer.android.com/training/search/setup.html
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.search_actions, menu);

    // Associate searchable configuration with the SearchView
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    searchView.setIconifiedByDefault(false);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case R.id.action_voice:
        doVoice();
        return true;

      case R.id.action_clear:
        return true;
    }

    return false;
  }

  private static final int ACTIVITY_VOICE = 1000;

  private void doVoice() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName())
        .putExtra(RecognizerIntent.EXTRA_PROMPT, getText(R.string.voice_prompt))
        .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

    startActivityForResult(intent, ACTIVITY_VOICE);
  }

  private void handleIntent(Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      String query = intent.getStringExtra(SearchManager.QUERY);
      Log.d(LOG, "Query: " + query);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case ACTIVITY_VOICE:
        if (resultCode == RESULT_OK) {
          /*
          int i = 0;
          float[] confidence = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);
          List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
          Log.d(LOG, confidence.length + ":" + results.size());
          for (String str : results) {
            results.set(i, str);
            i++;
          }
          setItems(results);
          Log.d(LOG, "Confidence: " + confidence[0]);

          // TODO(burdon): Abort auto post if not confident.
          NetProto.Request request = NetProto.Request.newBuilder()
              .setHeader(NetProto.Request.Header.newBuilder())
              .addExtension(ActionProto.action, ActionProto.Action.newBuilder().setCommand(results.get(0)).build())
              .build();

          // TODO(burdon): Render items in new list (or graph).
          proxy.doAsyncPost(request, new Proxy.Callback() {
            @Override
            public void onResponse(NetProto.Response response) {
              Toast.makeText(MainActivity.this, "OK", Toast.LENGTH_LONG).show();
              if (response.hasExtension(QueryProto.result)) {
                QueryProto.Result result = response.getExtension(QueryProto.result, 0);
                List<String> items = new ArrayList<String>();
                for (ItemProto.Item item : result.getItemList()) {
                  String title = item.getData().getExtension(ItemProto.summary).getTitle();
                  items.add(title);
                }
                setItems(items);
              }
            }
          });
        }
        */
        break;
    }
  }}
}
