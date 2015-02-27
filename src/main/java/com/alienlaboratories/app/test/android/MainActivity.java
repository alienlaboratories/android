// Copyright (c) 2013 Alien Laboratories, Inc.

package com.alienlaboratories.app.test.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Main activity.
 */
public final class MainActivity extends Activity {

  private static final String LOG = MainActivity.class.getSimpleName();

  private String[] viewTitles;

  // https://developer.android.com/reference/android/support/v4/widget/DrawerLayout.html
  private DrawerLayout drawerLayout;

  private ListView drawerList;

  // http://developer.android.com/guide/components/fragments.html

  public static class ListViewFragment extends ListFragment {

  }

  static final String[] VIEWS = {"A", "B", "C"};

  // TODO(burdon): Create list fragment, debug fragment, etc.
  // https://developer.android.com/training/implementing-navigation/nav-drawer.html
  private class DrawerItemClickListener implements ListView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
      selectNavItem(position);
    }

    /** Swaps fragments in the main content view */
    private void selectNavItem(int position) {
      // Create a new fragment and specify the planet to show based on position.
      Fragment fragment = new ListViewFragment();
      Bundle args = new Bundle();
      args.putInt("key", position);
      fragment.setArguments(args);

      // Insert the fragment by replacing any existing fragment.
      getFragmentManager()
          .beginTransaction()
          .replace(R.id.content_frame, fragment)
          .commit();

      // Highlight the selected item, update the title, and close the drawer
      drawerList.setItemChecked(position, true);
      setTitle(VIEWS[position]);
      drawerLayout.closeDrawer(drawerList);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // TODO(burdon): Auth.
    // http://blog.notdot.net/2010/05/Authenticating-against-App-Engine-from-an-Android-app

    // Prefs.
//  PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false);

    // TODO(burdon): Configure navigation drawer
    // https://developer.android.com/design/patterns/navigation-drawer.html
    // https://developer.android.com/training/implementing-navigation/nav-drawer.html
    viewTitles = getResources().getStringArray(R.array.views);
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawerList = (ListView) findViewById(R.id.left_drawer);
    drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, viewTitles));
    drawerList.setOnItemClickListener(new DrawerItemClickListener());

    // TODO(burdon): Dummy data. Set model which should be configured to use proxy.
    // setItems(new ArrayList<String>(Lists.newArrayList("A", "B", "C")));
    // TODO(burdon): Move to ListFragment
    /*
    getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(LOG, "Item: " + i);
      }
    });
    */
  }

  @Override
  public void setTitle(CharSequence title) {
    getActionBar().setTitle(title);
  }

  // http://developer.android.com/training/search/setup.html
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_actions, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      // NOTE: Intent.ACTION_SEARCH could be handled by other apps.
      case R.id.action_search:
        startActivity(new Intent(this, SearchActivity.class));
        return true;

      case R.id.action_debug:
        startActivity(new Intent(this, DebugActivity.class));
        return true;

      case R.id.action_settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  // TODO(burdon): Move to ListFragment
  @SuppressWarnings("unchecked")
  protected void setItems(@Nullable List<String> items) {
    if (items == null) {
      items = Collections.EMPTY_LIST;
    }

    //setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
  }
}
