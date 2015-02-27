// Copyright 2012 Android Laboratories, Inc.

package com.alienlaboratories.app.test.android;

import android.app.Activity;
import android.os.Bundle;

/**
 *
 */
public final class DebugActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }
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
