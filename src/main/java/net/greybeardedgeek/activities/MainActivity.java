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

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notice that setContentView() is not used, because we use the root
        // android.R.id.content as the container for each fragment

        // setup action bar for tabs
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Tab tab = actionBar.newTab()
                .setText("Favorites")
                .setTabListener(new TabListener<LocationsFragment>(this, LocationsFragment.Filter.favorites.name(), LocationsFragment.class));
        actionBar.addTab(tab);


        tab = actionBar.newTab()
                .setText("Recent")
                .setTabListener(new TabListener<LocationsFragment>(this, LocationsFragment.Filter.recent.name(), LocationsFragment.class));
        actionBar.addTab(tab);


        tab = actionBar.newTab()
                .setText("All")
                .setTabListener(new TabListener<LocationsFragment>(this, LocationsFragment.Filter.all.name(), LocationsFragment.class));
        actionBar.addTab(tab);
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

    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private Fragment mFragment;

        public TabListener(Activity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {

            if (mFragment == null) {
                Bundle bundle = new Bundle();
                bundle.putString("filter", mTag);
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), bundle);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }

        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }
}
