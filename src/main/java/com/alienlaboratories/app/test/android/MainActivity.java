// Copyright (c) 2013 Alien Laboratories, Inc.

package com.alienlaboratories.app.test.android;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.alienlaboratories.app.test.android.data.Proxy;
import com.alienlaboratories.db.ItemProto;
import com.alienlaboratories.db.action.ActionProto;
import com.alienlaboratories.db.query.QueryProto;
import com.alienlaboratories.net.NetProto;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test activity (minimal app).
 */
public final class MainActivity extends ListActivity {

  private static final String LOG = MainActivity.class.getSimpleName();

  /*
  private final Injector injector = Guice.createInjector(new TestTypeModule());
  private final Proxy proxy = injector.getInstance(Proxy.class);
  */

  // TODO(burdon): Inject.
  private final Proxy proxy = Proxy.getInstance();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // TODO(burdon): Auth.
    // http://blog.notdot.net/2010/05/Authenticating-against-App-Engine-from-an-Android-app

    // Prefs.
//  PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false);

    // TODO(burdon): Click.
    getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(LOG, "Item: " + i);
      }
    });
  }

  // http://developer.android.com/training/search/setup.html
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_actions, menu);

    // Associate searchable configuration with the SearchView.
    /*
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    */

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case R.id.action_search:
//      onSearchRequested();
        return true;

      case R.id.action_voice:
        doVoice();
        return true;

      case R.id.action_clear:
        setItems(null);
        return true;

      case R.id.action_settings:
        startActivityForResult(new Intent(this, SettingsActivity.class), ACTIVITY_RESULT_SETTINGS);
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private static final int ACTIVITY_RESULT_SETTINGS = 1001;
  private static final int ACTIVITY_RESULT_VOICE = 1002;

  private void doVoice() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName())
        .putExtra(RecognizerIntent.EXTRA_PROMPT, getText(R.string.voice_prompt))
        .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

    startActivityForResult(intent, ACTIVITY_RESULT_VOICE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case ACTIVITY_RESULT_VOICE:
        if (resultCode == RESULT_OK) {
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
        break;
    }
  }

  @SuppressWarnings("unchecked")
  protected void setItems(@Nullable List<String> items) {
    if (items == null) {
      items = Collections.EMPTY_LIST;
    }

    // TODO(burdon): Change adapter.
    setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
  }

  // TODO(burdon): Move to diagnostics class.
  /*
  private final class UI {
//  final EditText message = (EditText) findViewById(R.id.edit_message);
    final TextView status = (TextView) findViewById(R.id.status);
    final Button test = (Button) findViewById(R.id.button_test);
  }

  UI ui;
  */

  /*
    // Get the intent, verify the action and get the query
    Intent intent = getIntent();
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      String query = intent.getStringExtra(SearchManager.QUERY);
      doSearch(query);
    }

    setContentView(R.layout.activity_main);
    ui = new UI();

    // TODO(burdon): Add items to list.
    ui.test.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        final String message = StringUtil.toString(ui.message.getText().toString());
        ui.status.setText(message);

        // TODO(burdon): Use real async. Threads?
        // http://developer.android.com/guide/components/processes-and-threads.html#Threads
        final String message = "OK";
        TestExecutor exec = new TestExecutor();
        exec.enqueue(new Runnable() {
          @Override
          public void run() {
            ui.status.setText(message);
            Log.d(LOG, message);
          }
        });
        exec.runAll();
      }
    });
  */
}
