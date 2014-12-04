package com.example.rachel.wygt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Rachel on 11/15/14.
 */
public class EditMyLocationActivity extends FragmentActivity {
    private GoogleMap googleMap;
    private GPSTracker gpsTracker = MyApplication.gpsTracker;
    private LatLng location, currentLocation;
    private Marker _marker;
    private String name, address;
    private final String LOGTAG = "EditMyLocationActivity";
    private EditText nameEdit, addressEdit;
    private String destinationDuration, destinationDistance, destDistMeters, destDistSeconds;
    private long id;
    private MyLocationDataSource locationDataSource = MyApplication.getMyLocationDataSource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
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
            Location _location = gpsTracker.getLocation();
            currentLocation = new LatLng(_location.getLatitude(), _location.getLongitude());
            (new GetDrivingDistance()).execute(location, currentLocation);
        }
        initializeMap();
        initViews();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_location_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_task_location:
                setTaskForLocation();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.menu_settings:
                Intent intent1 = new Intent(this, UserSettingsActivity.class);
                startActivity(intent1);
                return true;
            case R.id.list:
                Intent intent2 = new Intent(this, TaskListActivity.class);
                startActivity(intent2);
                return true;
            case R.id.listLocations:
                Intent intent3 = new Intent(this, MyLocationsActivity.class);
                startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setTaskForLocation() {
        address = addressEdit.getText().toString();
        name = nameEdit.getText().toString();
        Intent intent = new Intent(EditMyLocationActivity.this, DoSomethingActivity.class);
        intent.putExtra("Destination", name);
        intent.putExtra("Destination_location", location);
        intent.putExtra("Current_Location", currentLocation);
        intent.putExtra("Distance", destinationDistance);
        intent.putExtra("Duration", destinationDuration);
        intent.putExtra("DistanceMeters", destDistMeters);
        intent.putExtra("DurationSeconds", destDistSeconds);
        startActivity(intent);
    }

    public void initViews() {
        LinearLayout all = (LinearLayout) findViewById(R.id.edit_location_layout);
        LinearLayout display = (LinearLayout) findViewById(R.id.edit_location_display_layout);
        LinearLayout name = (LinearLayout) findViewById(R.id.edit_location_name_layout);
        LinearLayout address = (LinearLayout) findViewById(R.id.edit_location_address_layout);
        LinearLayout map = (LinearLayout) findViewById(R.id.edit_location_map_layout);
        LinearLayout buttons = (LinearLayout) findViewById(R.id.edit_location_button_layout);


        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) map.getLayoutParams();
        params.height = getPxByPercentage(.50);
        all.updateViewLayout(map, params);

        display.setMinimumHeight(getPxByPercentage(.20));
        name.setMinimumHeight(getPxByPercentage(.15));
        address.setMinimumHeight(getPxByPercentage(.15));
        //  map.setMinimumHeight(getPxByPercentage(.50));
        buttons.setMinimumHeight(getPxByPercentage(.12));

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

    public String getDestDistSeconds() {
        return destDistSeconds;
    }

    public void setDestDistSeconds(String destDistSeconds) {
        this.destDistSeconds = destDistSeconds;
    }

    public String getDestinationDuration() {
        return destinationDuration;
    }

    public void setDestinationDuration(String destinationDuration) {
        this.destinationDuration = destinationDuration;
    }

    public String getDestinationDistance() {
        return destinationDistance;
    }

    public void setDestinationDistance(String destinationDistance) {
        this.destinationDistance = destinationDistance;
    }

    public String getDestDistMeters() {
        return destDistMeters;
    }

    public void setDestDistMeters(String destDistMeters) {
        this.destDistMeters = destDistMeters;
    }

    public void saveMyLocation(View v) {
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


    public int getPxByPercentage(double percentage) {

        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float height = MyApplication.getHeight();
        float px = height * (metrics.densityDpi / 160f);

        return (int) (px * percentage);
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
                    if (_marker != null) {
                        _marker.remove();
                    }
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

    private class GetDrivingDistance extends AsyncTask<LatLng, LatLng, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            pDialog = new ProgressDialog(MyActivity.this);
//        pDialog.setMessage("Calculating Distance ...");
//        pDialog.setIndeterminate(false);
//        pDialog.setCancelable(true);
//        pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(LatLng... params) {
            Thread.currentThread().setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND + android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
            LatLng _origin = params[0];
            LatLng _destination = params[1];
            String origin = _origin.latitude + "," + _origin.longitude;
            String destination = _destination.latitude + "," + _destination.longitude;
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" +
                    origin + "&destinations=" + destination +
                    "&mode=driving&sensor=false&language=en-EN&units=imperial";
            Double finalDistance = 0.0;


            JSONObject _jsonObject = null;
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
                StatusLine status = httpResponse.getStatusLine();
                if (status.getStatusCode() != 200) {
                    Log.d("**DistanceMatrixHelper**", "HTTP error, invalid server status code: " + httpResponse.getStatusLine());
                }
                HttpEntity httpEntity = httpResponse.getEntity();
                String response = EntityUtils.toString(httpEntity);
                Log.d("RESPONSE", response);
                _jsonObject = new JSONObject(response);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return _jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            String duration = "";
            JSONArray rows = null;
            List<String> distances = new ArrayList<String>();
            if (jsonObject != null) {
                try {
                    rows = jsonObject.getJSONArray("rows");
                    for (int i = 0; i < rows.length(); i++) {
                        JSONObject row = rows.getJSONObject(i);
                        JSONArray elements = row.getJSONArray("elements");
                        for (int j = 0; i < elements.length(); i++) {
                            JSONObject element = elements.getJSONObject(0);
                            JSONObject _duration = element.getJSONObject("duration");
                            duration = _duration.getString("text");
                            EditMyLocationActivity.this.destDistSeconds = _duration.getString("value");
                            JSONObject distance = element.getJSONObject("distance");

                            EditMyLocationActivity.this.destDistMeters = distance.getString("value");
                            distances.add(distance.getString("text"));
                        }
                        String _distances = null;
                        for (String distance : distances) {
                            _distances = distance;
                        }
                        String distance = _distances;

                        EditMyLocationActivity.this.setDestinationDistance(distance);
                        EditMyLocationActivity.this.setDestinationDuration(duration);


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }


}

