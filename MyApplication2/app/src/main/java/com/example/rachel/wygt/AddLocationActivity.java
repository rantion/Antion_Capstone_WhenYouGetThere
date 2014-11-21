package com.example.rachel.wygt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.util.concurrent.ExecutionException;

/**
 * Created by Rachel on 11/11/14.
 */
public class AddLocationActivity extends Activity {
    private String LOGTAG = "AddLocationActivity";
    private GoogleMap googleMap;
    private GPSTracker gpsTracker = MyApplication.gpsTracker;
    private Marker _marker;
    private LatLng location;
    private String address, name;
    private MyLocationDataSource loctionDataSource = MyApplication.getMyLocationDataSource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        initializeMap();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveLocation(View v){
        EditTextClear getName = (EditTextClear) findViewById(R.id.save_location_name);
        String name = getName.getText().toString();
        loctionDataSource.createMyLocation(name, address, location.latitude, location.longitude);
        Toast.makeText(this, "Location Saved!",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MyLocationsActivity.class);
        startActivity(intent);

    }

    public void getAddressAddLoc(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        Log.d(LOGTAG, "getAddress clicked");
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.JELLY_BEAN
                &&
                Geocoder.isPresent()) {
            EditText _location = (EditText) findViewById(R.id.enter_my_location_field);
            String locationName = _location.getText().toString();
            if (locationName.length() > 0) {
                (new GetAddressesFromName()).execute(locationName);
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


    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.add_location_map)).getMap();
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            Location current = gpsTracker.getLocation();

            if (current != null) {
                LatLng currentPos = new LatLng(current.getLatitude(), current.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 12.0f));
            }
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
            MarkerOptions marker = new MarkerOptions();
            LatLng currentLoc =  new LatLng(current.getLatitude(), current.getLongitude());
            marker.position(currentLoc).title("Current Location");
            _marker = googleMap.addMarker(marker);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                   currentLoc).zoom(14).build();
            _marker.setSnippet("Current Location");
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
    }


    private class GetAddressesFromName extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... params) {
            long threadId = Thread.currentThread().getId();
            Log.d(LOGTAG, "doInBackground Thread ID: " + threadId);
            String placeName = params[0];
            List<Address> addressList = null;

            Geocoder geocoder = new Geocoder(AddLocationActivity.this, Locale.US); //context of class wrapped in
            try {
                addressList = geocoder.getFromLocationName(placeName, 10);

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
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddLocationActivity.this);
                    builder.setTitle("Select your address: ");

                    builder.setItems(_addresses, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            Address selectedAddress = addressArray[which];
                            final LatLng destinationLocation = new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude());
                            MarkerOptions marker = new MarkerOptions();
                            marker.position(destinationLocation).title(selectedAddress.getAddressLine(0));
                            _marker = googleMap.addMarker(marker);
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                                    destinationLocation).zoom(14).build();
                            _marker.setSnippet((String) _addresses[which]);
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                           AddLocationActivity.this.location = destinationLocation;
                           AddLocationActivity.this.address = (String)_addresses[which];
                        }


                    });
                    builder.show();
                }
            }
        }
    }
}
