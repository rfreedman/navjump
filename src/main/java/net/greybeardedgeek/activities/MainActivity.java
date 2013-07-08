package net.greybeardedgeek.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.greybeardedgeek.R;
import net.greybeardedgeek.fragments.AddEditLocationDialogFragment;
import net.greybeardedgeek.fragments.LocationsFragment;
import net.greybeardedgeek.fragments.LocationsFragment.Filter;

public class MainActivity extends Activity {
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notice that setContentView() is not used, because we use the root
        // android.R.id.content as the container for each fragment

        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(createTab("Favorites", Filter.favorites));
        actionBar.addTab(createTab("Recent", Filter.recent));
        actionBar.addTab(createTab("All", Filter.all));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(net.greybeardedgeek.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = false;

        switch (item.getItemId()) {
            case R.id.action_add_location:
                showAddDialog();
                handled = true;
                break;

            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            default:
                handled = super.onOptionsItemSelected(item);
                break;
        }

        return handled;
    }

    private void showAddDialog() {
        AddEditLocationDialogFragment.newInstance().show(getFragmentManager(), "addDialog");
    }

    private Tab createTab(String title, LocationsFragment.Filter filter) {
        Tab tab = actionBar.newTab()
                .setText(title)
                .setTabListener(new TabListener<LocationsFragment>(this, filter.name(), LocationsFragment.class));

        return tab;
    }

    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity activity;
        private final String tag;
        private final Class<T> fragmentClass;
        private Fragment fragment;

        public TabListener(Activity activity, String tag, Class<T> fragmentClass) {
            this.activity = activity;
            this.tag = tag;
            this.fragmentClass = fragmentClass;
        }

        public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
            if (fragment == null) {
                Bundle bundle = new Bundle();
                bundle.putString("filter", tag);
                fragment = Fragment.instantiate(activity, fragmentClass.getName(), bundle);
                fragmentTransaction.add(android.R.id.content, fragment, tag);
            } else {
                fragmentTransaction.attach(fragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {
            if (fragment != null) {
                fragmentTransaction.detach(fragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {
        }
    }
}
