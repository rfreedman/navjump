package net.greybeardedgeek.fragments;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.greybeardedgeek.R;
import net.greybeardedgeek.database.LocationProvider.Locations;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddEditLocationDialogFragment extends DialogFragment implements LocationListener {

    private EditText nameView;
    private EditText addressView;
    private EditText latitudeView;
    private EditText longitudeView;

    private double lastLatitude;
    private double lastLongitude;

    private long editItemId = -1L;

    public static AddEditLocationDialogFragment newInstance() {
        AddEditLocationDialogFragment fragment = new AddEditLocationDialogFragment();
        return fragment;
    }

    public static AddEditLocationDialogFragment newInstance(long itemId) {
        AddEditLocationDialogFragment fragment = new AddEditLocationDialogFragment();

        Bundle args = new Bundle();
        args.putLong("itemId", itemId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null && args.containsKey("itemId")) {
            editItemId = args.getLong("itemId");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_location_dialog, container, false);

        nameView = (EditText) view.findViewById(R.id.name);
        addressView = (EditText) view.findViewById(R.id.address);
        longitudeView = (EditText) view.findViewById(R.id.longitude);
        latitudeView = (EditText) view.findViewById(R.id.latitude);

        view.findViewById(R.id.latLongButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lastLatitude == 0.0 && lastLongitude == 0.0) {
                    Toast.makeText(getActivity(), "Location Not Available", Toast.LENGTH_SHORT).show();
                } else {
                    latitudeView.setText(new Double(lastLatitude).toString());
                    longitudeView.setText(new Double(lastLongitude).toString());
                    addressView.setText(getAddress(lastLatitude, lastLongitude));
                }
            }
        });

        view.findViewById(R.id.positiveButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(editItemId > -1) {
                    updateLocation();
                } else {
                    addLocation();
                }
            }
        });

        view.findViewById(R.id.negativeButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        if(editItemId > -1) {
            getDialog().setTitle("Update Location");
            ((TextView)view.findViewById(R.id.positiveButton)).setText("Update");
            populateItem();
        } else {
            getDialog().setTitle("Add Location");
            ((TextView)view.findViewById(R.id.positiveButton)).setText("Add");

        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, this);
        } catch(Exception ex) {
            // network provider doesn't exist
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        } catch(Exception ex) {
            // gps provider doesn't exist
        }

    }

    @Override
    public void onPause() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);
        super.onPause();
    }

    private String getAddress(double latitude, double longitude) {
        String addressString = "";

        try {
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if(addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                int i = 0;
                StringBuilder sb = new StringBuilder()
                        .append(address.getAddressLine(i)).append(" ")
                        .append(address.getLocality()).append(" ")
                        .append(address.getAdminArea()).append(" ")
                        .append(address.getPostalCode());

                addressString = sb.toString();
            }

        } catch(Exception ex) {
            Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_SHORT).show();
        }

        return addressString;
    }

    private String getAddress(String latString, String longString) {
        String address = "";
        try {
            address =  getAddress(Double.parseDouble(latString), Double.parseDouble(longString));
        } catch(NumberFormatException ex) {
            //
        }
        return address;
    }


    private void populateItem() {

        String[] projection = null;
        String selection = Locations.ID + " = ?";
        String idString = Long.toString(editItemId);
        String[] selectionArgs = {idString};
        String sort =  Locations.ID + " asc";

        Cursor cursor = getActivity().getContentResolver().query(Locations.CONTENT_URI, projection, selection, selectionArgs, sort);
        if(cursor != null && cursor.moveToFirst()) {
            nameView.setText(cursor.getString(cursor.getColumnIndex(Locations.NAME)));
            addressView.setText(cursor.getString(cursor.getColumnIndex(Locations.ADDRESS)));
            latitudeView.setText(cursor.getString(cursor.getColumnIndex(Locations.LATITUDE)));
            longitudeView.setText(cursor.getString(cursor.getColumnIndex(Locations.LONGITUDE)));
        }
        cursor.close();
    }

    private void updateLocation() {
        ContentValues locationValues = new ContentValues();
        locationValues.put(Locations.NAME, nameView.getText().toString());
        locationValues.put(Locations.LATITUDE, latitudeView.getText().toString());
        locationValues.put(Locations.LONGITUDE, longitudeView.getText().toString());

        String addressString = addressView.getText().toString();
        if(addressString == null || addressString.isEmpty()) {
            addressString = getAddress(latitudeView.getText().toString(), longitudeView.getText().toString());
        }

        locationValues.put(Locations.ADDRESS, addressString);

        if(validateLocation(locationValues)) {

            String idString = Long.toString(editItemId);
            String where = Locations.ID + " = ?";
            String[] selectionArgs = {idString};

            getActivity().getContentResolver().update(Locations.CONTENT_URI, locationValues, where, selectionArgs);
            dismiss();
        }
    }

    private void addLocation() {

        ContentValues locationValues = new ContentValues();
        locationValues.put(Locations.NAME, nameView.getText().toString());
        locationValues.put(Locations.LATITUDE, latitudeView.getText().toString());
        locationValues.put(Locations.LONGITUDE, longitudeView.getText().toString());
        locationValues.put(Locations.IS_FAVORITE, false);
        locationValues.put(Locations.LAST_USED, new Date().getTime());

        String addressString = addressView.getText().toString();
        if(addressString == null || addressString.isEmpty()) {
            addressString = getAddress(latitudeView.getText().toString(), longitudeView.getText().toString());
        }

        locationValues.put(Locations.ADDRESS, addressString);

        if(validateLocation(locationValues)) {
            getActivity().getContentResolver().insert(Locations.CONTENT_URI, locationValues);
            dismiss();
        }
    }


    private boolean validateLocation(ContentValues contentValues) {
        String errorMsg = null;

        do {
            if(!hasData(contentValues, Locations.NAME)) {
                errorMsg = "Location Name is required";
                break;
            }

            if(!hasData(contentValues, Locations.ADDRESS)) {
                if(!hasData(contentValues, Locations.LATITUDE) || !hasData(contentValues, Locations.LONGITUDE)) {
                    errorMsg = "Either Address or Latitude and Longitude are required";
                    break;
                }
            }

            if(hasData(contentValues, Locations.LATITUDE) || hasData(contentValues, Locations.LONGITUDE)) {
                try {
                    Double.parseDouble(contentValues.getAsString(Locations.LATITUDE));
                    Double.parseDouble(contentValues.getAsString(Locations.LONGITUDE));
                } catch (NumberFormatException ex) {
                    errorMsg = "Lat and Long must be numeric";
                    break;
                }
            }


        } while(false);

        if(errorMsg == null) {
            return true;
        } else {
            Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean hasData(ContentValues values, String key) {
        String value = values.getAsString(key);
        return hasData(value);
    }

    private boolean hasData(String value) {
        return value != null && !value.isEmpty();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLatitude = location.getLatitude();
        lastLongitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
