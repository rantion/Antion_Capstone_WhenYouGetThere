package com.example.rachel.wygt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Rachel on 11/15/14.
 */
public class EditMyLocationActivity extends FragmentActivity {
    private GoogleMap googleMap;
    private LatLng location;
    private Marker _marker;
    private String name, address;
    private final String LOGTAG = "EditMyLocationActivity";
    private EditText nameEdit, addressEdit;
    private long id;
    private MyLocationDataSource locationDataSource = MyApplication.getMyLocationDataSource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crud_my_location);
        nameEdit = (EditText) findViewById(R.id.edit_location_name);
        addressEdit = (EditText) findViewById(R.id.edit_location_address);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            double latitude = extras.getDouble("latitude");
            double longitude = extras.getDouble("longitude");
            location = new LatLng(latitude, longitude);
            name = extras.getString("name");
            address = extras.getString("address");
            id = extras.getLong("id");
            nameEdit.setText(name);
            addressEdit.setText(address);
        }
        initializeMap();
        addressEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void initializeMap() {
        Log.d("Marker", "Initialize Map Called");
        if (googleMap == null) {
            Log.d("Marker", "Map is Null");
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.edit_location_map)).getMap();
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            if (location != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f));
                MarkerOptions marker = new MarkerOptions();
                marker.position(location).title("Current Location");
                _marker = googleMap.addMarker(marker);
                CameraPosition cameraPosition = new CameraPosition.Builder().target(
                        location).zoom(14).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(this,
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void clearName(View view) {
        nameEdit.setText("");
    }

    public void clearAddress(View view) {
        addressEdit.setText("");
    }

    public void findAddressOnMap(View view) {
        String address = addressEdit.getText().toString();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.JELLY_BEAN
                &&
                Geocoder.isPresent()) {
            if (address.length() > 0) {
                (new GetAddressesFromName()).execute(address);
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Please Enter A Destination")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            }
        }
    }

    public void saveMyLocation(View v){
        address = addressEdit.getText().toString();
        name = nameEdit.getText().toString();
        MyLocation myLocation = locationDataSource.getLocationById(id);
        myLocation.setLatitude(location.latitude);
        myLocation.setLongitude(location.longitude);
        myLocation.setAddress(address);
        myLocation.setName(name);
        Toast.makeText(this, "location updated", Toast.LENGTH_SHORT).show();
        locationDataSource.updateMyLocation(myLocation);
    }

    private class GetAddressesFromName extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... params) {
            long threadId = Thread.currentThread().getId();
            Log.d(LOGTAG, "doInBackground Thread ID: " + threadId);
            String placeName = params[0];
            List<Address> addressList = null;

            Geocoder geocoder = new Geocoder(EditMyLocationActivity.this, Locale.US); //context of class wrapped in
            try {
                addressList = geocoder.getFromLocationName(placeName, 1);

            } catch (IOException e) {
                e.printStackTrace();
                addressList = new ArrayList<Address>();
            }
            return addressList;
        }

        @Override
        protected void onPostExecute(final List<Address> addresses) {
            if (addresses != null) {
                long threadId = Thread.currentThread().getId();
                Log.d(LOGTAG, "onPostExecute threadID: " + threadId);
                final CharSequence[] _addresses = new CharSequence[addresses.size()];
                int j = 0;
                final Address[] addressArray = new Address[addresses.size()];
                for (Address address : addresses) {
                    int lastIndex = address.getMaxAddressLineIndex();
                    String addressLine = "";
                    for (int i = 0; i <= lastIndex; i++) {
                        if (i != lastIndex) {
                            addressLine = addressLine + address.getAddressLine(i) + "\n";
                        } else {
                            addressLine = addressLine + address.getAddressLine(i);
                        }
                    }
                    addressArray[j] = address;
                    _addresses[j] = addressLine;
                    j++;
                }
                if (addresses.size() == 0) {
                } else {
                    Address selectedAddress = addressArray[0];
                    final LatLng destinationLocation = new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude());
                    location = destinationLocation;
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(destinationLocation).title(selectedAddress.getAddressLine(0));
                    _marker = googleMap.addMarker(marker);
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            destinationLocation).zoom(14).build();
                    _marker.setSnippet((String) _addresses[0]);
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    EditMyLocationActivity.this.location = destinationLocation;
                    EditMyLocationActivity.this.address = (String) _addresses[0];
                    addressEdit.setText(address);
                }


            }
        }
    }
}

