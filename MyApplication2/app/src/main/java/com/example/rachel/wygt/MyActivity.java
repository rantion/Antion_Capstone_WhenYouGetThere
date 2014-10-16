package com.example.rachel.wygt;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnKeyListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class MyActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener, OnKeyListener {

    String _logTag = "*** WYGT MainActivity ***";
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    private LocationClient locationClient;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    LocationRequest mLocationRequest;
    boolean mUpdatesRequested;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;
    private TextView mAddress;
    private ProgressBar mActivityIndicator;
    private Location mLocation;
    private GoogleMap googleMap;
    private Marker _marker;
    private MapFragment mf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        initializeMap();
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        confirmNetworkProviderAvailable(lm);
        servicesConnected();
        EditText addressBox = (EditText) findViewById(R.id.enter_location_field);
        addressBox.setOnKeyListener(this);

        mPrefs = getSharedPreferences("SharedPreferences",
                Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        locationClient = new LocationClient(this, this, this);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mActivityIndicator =
                (ProgressBar) findViewById(R.id.address_progress);
    }

    public void getAddress(View v) {
        Log.d(_logTag, "getAddress clicked");
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.JELLY_BEAN
                &&
                Geocoder.isPresent()) {
            mActivityIndicator.setVisibility(View.VISIBLE);
            EditText _location = (EditText) findViewById(R.id.enter_location_field);
            String locationName = _location.getText().toString();
            (new GetAddressesFromName()).execute(locationName);
        }
    }

    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            mf =((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map));


            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location current = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (current != null) {
                LatLng currentPos = new LatLng(current.getLatitude(), current.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 12.0f));
            }
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        locationClient.connect();
//    }
//
//    @Override
//    protected void onStop() {
//        // If the client is connected
//        if (locationClient.isConnected()) {
//            locationClient.removeLocationUpdates((com.google.android.gms.location.LocationListener) this);
//            /*
//             * Remove location updates for a listener.
//             * The current Activity is the listener, so
//             * the argument is "this".
//             */
//        }
//        /*
//         * After disconnect() is called, the client is
//         * considered "dead".
//         */
//        locationClient.disconnect();
//        super.onStop();
//    }
//
//    @Override
//    protected void onPause() {
//        // Save the current setting for updates
//        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
//        mEditor.commit();
//        super.onPause();
//    }
//
//    @Override
//    protected void onResume() {
//        /*
//         * Get any previous setting for location updates
//         * Gets "false" if an error occurs
//         */
//        if (mPrefs.contains("KEY_UPDATES_ON")) {
//            mUpdatesRequested =
//                    mPrefs.getBoolean("KEY_UPDATES_ON", false);
//
//            // Otherwise, turn off location updates
//        } else {
//            mEditor.putBoolean("KEY_UPDATES_ON", false);
//            mEditor.commit();
//        }
//    }
//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_getCurrentLocation) {
            onMenuFromLocation(item);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onMenuFromLocation(MenuItem item) {
        Log.d(_logTag, "from Location Menu selected");
        clearDisplay();
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (mLocation == null) {
            Log.d(_logTag, "no Last Location Available");
            return;
        }
        LatLng currentPos = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        MarkerOptions marker = new MarkerOptions();
        marker.position(currentPos).title("Current Location");
        googleMap.addMarker(marker);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                currentPos).zoom(12).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        (new GetAddressesFromLocation()).execute(mLocation);

    }

    boolean confirmNetworkProviderAvailable(LocationManager lm) {

        boolean networkAvailable = confirmAirplaneModeIsOff() &&
                confirmNetworkProviderEnabled(lm) &&
                confirmWifiAvailable();
        return networkAvailable;
    }

    public boolean confirmNetworkProviderEnabled(LocationManager lm) {
        boolean isAvailable = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isAvailable) {
            AlertUserDialog dialog = new AlertUserDialog("Please Enable Location Services", Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            dialog.show(getFragmentManager(), null);
        }
        return isAvailable;
    }

    public boolean confirmAirplaneModeIsOff() {

        Log.d(_logTag, "inside AirPlaneMode");
        boolean isOff =
                Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 0;
        Log.d(_logTag, "AirPlane Mode Is : " + isOff);
        if (!isOff) {
            AlertUserDialog dialog = new AlertUserDialog("Please disable Airplane mode", Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            dialog.show(getFragmentManager(), null);
        }
        return isOff;
    }

    public boolean confirmWifiAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isAvailable = wifiInfo.isAvailable();
        if (!isAvailable) {
            AlertUserDialog dialog = new AlertUserDialog("Please Enable Your WiFi", Settings.ACTION_WIFI_SETTINGS);
            dialog.show(getFragmentManager(), null);
        }
        return isAvailable;
    }

    private void clearDisplay() {
//        TextView textView = (TextView)findViewById(R.id.textView);
//        textView.setText("");
    }

    private void displayAddressLines(Address address) {
        int lastIndex = address.getMaxAddressLineIndex();
        for (int i = 0; i <= lastIndex; i++) {
            String addressLine = address.getAddressLine(i);
            addLineToDisplay(addressLine);
        }
        addLineToDisplay("");
    }

    private void addLineToDisplay(CharSequence displayLine) {
//        TextView textView = (TextView)findViewById(R.id.textView);
//
//        CharSequence existingText = textView.getText();
//        CharSequence newText = existingText + "\n"+ displayLine;
//
//        textView.setText(newText);

    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        // If already requested, start periodic updates
        if (mUpdatesRequested) {
            locationClient.requestLocationUpdates(mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        }
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.d(_logTag, String.valueOf(connectionResult.getErrorCode()));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated

        int latitude = (int) (mLocation.getLatitude() * 1e6);
        int longitude = (int) (mLocation.getLongitude() * 1e6);


        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    //
    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
//

        if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
                keyCode == EditorInfo.IME_ACTION_DONE ||
                event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

            if (!event.isShiftPressed()) {
                Log.v("AndroidEnterKeyActivity", "Enter Key Pressed!");
                switch (view.getId()) {
                    case R.id.enter_location_field:
                        getAddress(view);
                        break;
                }
                return true;
            }

        }
        return false; // pass on to other listeners.

    }


    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                    /*
                     * Try the request again
                     */

                        break;
                }

        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(),
                        "Location Updates");
            }
        }
        return false;
    }


    class GetAddressesFromName extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... params) {
            long threadId = Thread.currentThread().getId();
            Log.d(_logTag, "doInBackground Thread ID: " + threadId);
            String placeName = params[0];
            List<Address> addressList = null;

            Geocoder geocoder = new Geocoder(MyActivity.this); //context of class wrapped in
            try {
                addressList = geocoder.getFromLocationName(placeName, 10);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return addressList;
        }

        @Override
        protected void onPostExecute(final List<Address> addresses) {
            long threadId = Thread.currentThread().getId();
            Log.d(_logTag, "onPostExecute threadID: " + threadId);
            MyActivity.this.clearDisplay();
            final CharSequence[] _addresses = new CharSequence[addresses.size()];
            int j = 0;
            final Address[] addressArray = new Address[addresses.size()];
            for (Address address : addresses) {
                    MyActivity.this.displayAddressLines(address);
                int lastIndex = address.getMaxAddressLineIndex();
                String addressLine = "";
                for (int i = 0; i <= lastIndex; i++) {
                    addressLine = addressLine + address.getAddressLine(i) + "\n";
                }
                addressArray[j] = address;
                _addresses[j] = addressLine;
                j++;
            }
            if (addresses.size() == 0) {
                MyActivity.this.clearDisplay();
                MyActivity.this.addLineToDisplay("I'm sorry, we cannot find your location");
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyActivity.this);
                builder.setTitle("Select your address: ");

                builder.setItems(_addresses, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Address selectedAddress = addressArray[which];
                        LatLng currentPos = new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude());
                        if (_marker != null) {
                            _marker.remove();
                        }
                        MarkerOptions marker = new MarkerOptions();
                        marker.position(currentPos).title(selectedAddress.getAddressLine(0));
                        _marker = googleMap.addMarker(marker);
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                                currentPos).zoom(14).build();
                        _marker.setSnippet((String) _addresses[which]);
                        googleMap.setOnMarkerClickListener(MarkerListener);
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        mActivityIndicator.setVisibility(View.GONE);


                    }
                });
                builder.show();
            }
        }

        GoogleMap.OnMarkerClickListener MarkerListener = new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(MyActivity.this, CreateTaskActivity.class);
                intent.putExtra("Destination", marker.getSnippet());
                intent.putExtra("Destination_location", marker.getPosition());
                startActivity(intent);
                return false;
            }
        };
    }


    class GetAddressesFromLocation extends AsyncTask<Location, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(Location... params) {
            long threadId = Thread.currentThread().getId();
            Log.d(_logTag, "doInBackground Thread ID: " + threadId);
            Location location = params[0];
            List<Address> addressList = null;

            Geocoder geocoder = new Geocoder(MyActivity.this); //context of class wrapped in
            try {
                addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 2);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return addressList;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            mActivityIndicator.setVisibility(View.GONE);
            long threadId = Thread.currentThread().getId();
            Log.d(_logTag, "onPostExecute threadID: " + threadId);
            MyActivity.this.clearDisplay();
            for (Address address : addresses) {
                MyActivity.this.displayAddressLines(address);
            }
        }
    }


}