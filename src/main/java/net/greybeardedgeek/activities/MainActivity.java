package net.greybeardedgeek.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.greybeardedgeek.R;
import net.greybeardedgeek.database.LocationProvider.Locations;
import net.greybeardedgeek.fragments.AddLocationDialogFragment;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";

    private ListView locationList;
    private CursorAdapter locationAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationList = (ListView) findViewById(R.id.location_list);
        locationAdapter = new LocationAdapter(this);
        locationList.setAdapter(locationAdapter);

        locationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                handleItemClick(view);
            }
        });

        registerForContextMenu(locationList);

        getLoaderManager().initLoader(0, null, this);

        Log.d(TAG, "onCreate");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.location_context_menu, menu);
    }

    private void handleItemClick(View view) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        navigate(viewHolder);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        View targetView = info.targetView;
        ViewHolder viewHolder = (ViewHolder) targetView.getTag();

        switch (item.getItemId()) {
            case R.id.action_nav:
                navigate(viewHolder);
                return true;

            case R.id.action_delete:
                deleteOnConfirm(info.id,viewHolder.nameView.getText().toString());
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }


    private void navigate(ViewHolder viewHolder){
        double latitude = 0.0;
        double longitude = 0.0;

        try {
            if(viewHolder.latString != null) {
                latitude = Double.parseDouble(viewHolder.latString);
            }

            if(viewHolder.longString != null) {
                longitude = Double.parseDouble(viewHolder.longString);
            }

        } catch (NumberFormatException ex) {
        //
        }

        if(latitude != 0.0 && longitude != 0.0) {
            navigate(latitude, longitude);
        } else {
            navigate(viewHolder.address);
        }
    }

    private void navigate(String address) {
        try {
            Uri uri = Uri.parse("google.navigation:q=" + address.replaceAll(" ", "+"));
            Log.d("nav", "navigating using uri: " + uri);
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch(ActivityNotFoundException ex) {
            Toast.makeText(this, "Google Navigation Application Not Found", Toast.LENGTH_LONG).show();
        }
    }

    private void navigate(double latitude, double longitude) {
        try {
            Uri uri = Uri.parse("google.navigation:ll=" + latitude + "," + longitude);
            Log.d("nav", "navigating using uri: " + uri);
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch(ActivityNotFoundException ex) {
            Toast.makeText(this, "Google Navigation Application Not Found", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(net.greybeardedgeek.R.menu.main, menu);
	    return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = false;

        switch(item.getItemId()) {
            case R.id.action_add_location:
                showAddDialog();
                handled = true;
                break;

            default:
                handled =  super.onOptionsItemSelected(item);
                break;
        }

        return handled;
    }

    private void showAddDialog() {
        AddLocationDialogFragment.newInstance().show(getFragmentManager(), "addDialog");
    }

    private void deleteLocation(long locationId) {
        getContentResolver().delete(ContentUris.withAppendedId(Locations.CONTENT_URI, locationId), null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.d(TAG, "onCreateLoader");

        // TODO - this is ALL - implement favorites and recents

        String selectionCritera = null;
        String[] selectionArgs = null;

        CursorLoader loader =  new CursorLoader(
                this,
                Locations.CONTENT_URI,
                null, // projection
                selectionCritera, // selection
                selectionArgs, // selection parameters
                Locations.NAME + " asc " // sort
        );

        return loader;
    }

    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        locationAdapter.swapCursor(cursor);

        if(cursor == null) {
            Log.d(TAG, "onLoadFinished - cursor is null");
        } else {
            Log.d(TAG, "onLoadFinished - cursor count: " + cursor.getCount());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        locationAdapter.swapCursor(null);
    }

    private static class LocationAdapter extends CursorAdapter {

        public LocationAdapter(Context context) {
            super(context, null, 0);
        }

        public LocationAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_location, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.nameView = (TextView) view.findViewById(R.id.location_name);
            viewHolder.addressView = (TextView) view.findViewById(R.id.address);
            viewHolder.latlongView = (TextView) view.findViewById(R.id.latlong);
            viewHolder.favoriteView = (CheckBox) view.findViewById(R.id.star);
            view.setTag(viewHolder);
            return view;
        }

        @Override
        public void bindView(View view, final Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            viewHolder.id = cursor.getInt(cursor.getColumnIndex(Locations.ID));
            viewHolder.nameView.setText(cursor.getString(cursor.getColumnIndex(Locations.NAME)));

            viewHolder.address = cursor.getString(cursor.getColumnIndex(Locations.ADDRESS));
            viewHolder.latString = cursor.getString(cursor.getColumnIndex(Locations.LATITUDE));
            viewHolder.longString = cursor.getString(cursor.getColumnIndex(Locations.LONGITUDE));

            if(viewHolder.address != null && !viewHolder.address.isEmpty()) {
                viewHolder.addressView.setText(viewHolder.address);
                viewHolder.addressView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.addressView.setText("");
                viewHolder.addressView.setVisibility(View.GONE);
            }

            if(viewHolder.latString != null && !viewHolder.latString.isEmpty() && viewHolder.longString != null && !viewHolder.longString.isEmpty()) {
                viewHolder.latlongView.setText("(" + viewHolder.latString + ", " + viewHolder.longString + ")");
                viewHolder.latlongView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.latlongView.setText("");
                viewHolder.latlongView.setVisibility(View.GONE);
            }

            viewHolder.favoriteView.setChecked(cursor.getInt(cursor.getColumnIndex(Locations.IS_FAVORITE)) != 0);
            viewHolder.favoriteView.setTag(new Integer(viewHolder.id));
            viewHolder.favoriteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox checkBox = (CheckBox) view;
                    int id = ((Integer) checkBox.getTag()).intValue();
                    ContentValues values = new ContentValues();
                    values.put(Locations.IS_FAVORITE, checkBox.isChecked());
                    context.getContentResolver().update(ContentUris.withAppendedId(Locations.CONTENT_URI, id), values, null, null);
                }
            });
        }
    }

    private static class ViewHolder {
        public int id;
        public TextView nameView;
        public TextView addressView;
        public TextView latlongView;
        public CheckBox favoriteView;

        public String address;
        public String latString;
        public String longString;
    }

    private void deleteOnConfirm(final long locationId, final String locationName) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("Delete Location");
        dialog.setMessage("Delete " + locationName + "?");
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                deleteLocation(locationId);
                dialog.dismiss();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
    }
}

