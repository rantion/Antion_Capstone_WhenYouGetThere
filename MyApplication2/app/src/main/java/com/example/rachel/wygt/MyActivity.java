package com.example.rachel.wygt;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MyActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    String _logTag = "*** WYGT MainActivity ***";

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
    private NetworkProviderStatusReciever _statusReciever;
    private MyLocationListener _networkListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        servicesConnected();
        mPrefs = getSharedPreferences("SharedPreferences",
                Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        locationClient = new LocationClient(this,this,this);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mAddress = (TextView) findViewById(R.id.textView);
        mActivityIndicator =
                (ProgressBar) findViewById(R.id.address_progress);
    }

    public void getAddress(View v) {
        // Ensure that a Geocoder services is available
        Log.d(_logTag, "getAddress clicked");
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.JELLY_BEAN
                &&
                Geocoder.isPresent()) {
            // Show the activity indicator
//            mActivityIndicator.setVisibility(View.VISIBLE);
            /*
             * Reverse geocoding is long-running and synchronous.
             * Run it on a background thread.
             * Pass the current location to the background task.
             * When the task finishes,
             * onPostExecute() displays the address.
             */
            if(mLocation == null){
                LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
                mLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            EditText _location = (EditText)findViewById(R.id.enter_location_field);
            String locationName = _location.getText().toString();
            (new GetAddressesFromName()).execute(locationName);
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

    public void onMenuFromLocation(MenuItem item){
        Log.d(_logTag,"from Location Menu selected");
        clearDisplay();
        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        mLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(mLocation == null){
            Log.d(_logTag,"no Last Location Available");
            return;
        }
        (new GetAddressesFromLocation()).execute(mLocation);

//        Geocoder geocoder = new Geocoder(this);
//        try {
//            List<Address> addressList = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 5);
//            int addressesReturned = addressList.size();
//            Log.d(_logTag,"number of addresses returned: "+ addressesReturned);
//
//            for(Address address: addressList){
//                displayAddressLines(address);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
//    boolean confirmNetworkProviderAvailable (LocationManager lm) {
//
//        boolean networkAvailable =  confirmAirplaneModeIsOff()&&
//                confirmNetworkProviderEnabled(lm)&&
//                confirmWifiAvailable()
//                ;
//        return networkAvailable;
//    }
//
//    public boolean confirmNetworkProviderEnabled (LocationManager lm){
//        boolean isAvailable = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//        if(!isAvailable){
//            AlertUserDialog dialog = new AlertUserDialog("Please Enable Location Services" , Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            dialog.show(getFragmentManager(),null);
//        }
//        return isAvailable;
//    }
//
//    public boolean confirmAirplaneModeIsOff (){
//
//        Log.d(_logTag, "inside AirPlaneMode");
//        boolean isOff =
//                Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 0;
//        Log.d(_logTag, "AirPlane Mode Is : "+isOff);
//        if(!isOff){
//            AlertUserDialog dialog = new AlertUserDialog("Please disable Airplane mode", Settings.ACTION_AIRPLANE_MODE_SETTINGS);
//            dialog.show(getFragmentManager(),null);
//        }
//        return isOff;
//    }
//
//    public boolean confirmWifiAvailable(){
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        boolean isAvailable = wifiInfo.isAvailable();
//        if(!isAvailable){
//            AlertUserDialog dialog = new AlertUserDialog("Please Enable Your WiFi", Settings.ACTION_WIFI_SETTINGS);
//            dialog.show(getFragmentManager(),null);
//        }
//        return isAvailable;
//    }

    private void clearDisplay(){
        TextView textView = (TextView)findViewById(R.id.textView);
        textView.setText("");
    }

    private void displayAddressLines(Address address){
        int lastIndex = address.getMaxAddressLineIndex();
        for(int i = 0; i<=lastIndex; i++){
            String addressLine = address.getAddressLine(i);
            addLineToDisplay(addressLine);
        }
        addLineToDisplay("" );
    }

    private void addLineToDisplay(CharSequence displayLine){
        TextView textView = (TextView)findViewById(R.id.textView);

        CharSequence existingText = textView.getText();
        CharSequence newText = existingText + "\n"+ displayLine;

        textView.setText(newText);

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
            Log.d(_logTag,String.valueOf(connectionResult.getErrorCode()));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
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

            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
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

//    private class GetAddressTask extends
//            AsyncTask<Location, Void, String> {
//        Context mContext;
//
//        public GetAddressTask(Context context) {
//            super();
//            mContext = context;
//        }
//
//        @Override
//        protected String doInBackground(Location... params) {
//            Geocoder geocoder =
//                    new Geocoder(mContext, Locale.getDefault());
//            // Get the current location from the input parameter list
//            Location loc = params[0];
//            // Create a list to contain the result address
//            List<Address> addresses = null;
//            try {
//                /*
//                 * Return 1 address.
//                 */
//                addresses = geocoder.getFromLocation(loc.getLatitude(),
//                        loc.getLongitude(), 1);
//            } catch (IOException e1) {
//                Log.e("LocationSampleActivity",
//                        "IO Exception in getFromLocation()");
//                e1.printStackTrace();
//                return ("IO Exception trying to get address");
//            } catch (IllegalArgumentException e2) {
//                // Error message to post in the log
//                String errorString = "Illegal arguments " +
//                        Double.toString(loc.getLatitude()) +
//                        " , " +
//                        Double.toString(loc.getLongitude()) +
//                        " passed to address service";
//                Log.e("LocationSampleActivity", errorString);
//                e2.printStackTrace();
//                return errorString;
//            }
//            // If the reverse geocode returned an address
//            if (addresses != null && addresses.size() > 0) {
//                // Get the first address
//                Address address = addresses.get(0);
//                /*
//                 * Format the first line of address (if available),
//                 * city, and country name.
//                 */
//                String addressText = String.format(
//                        "%s, %s, %s",
//                        // If there's a street address, add it
//                        address.getMaxAddressLineIndex() > 0 ?
//                                address.getAddressLine(0) : "",
//                        // Locality is usually a city
//                        address.getLocality(),
//                        // The country of the address
//                        address.getCountryName());
//                // Return the text
//                return addressText;
//            } else {
//                return "No address found";
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String address) {
//            // Set activity indicator visibility to "gone"
//          //  mActivityIndicator.setVisibility(View.GONE);
//            // Display the results of the lookup.
//            mAddress.setText(address);
//        }
//    }

    class GetAddressesFromName extends AsyncTask<String, Void, List<Address>>{

        @Override
        protected List<Address> doInBackground(String... params) {
            long threadId = Thread.currentThread().getId();
            Log.d(_logTag,"doInBackground Thread ID: "+threadId);
            String placeName = params[0];
            List<Address> addressList = null;

            Geocoder geocoder = new Geocoder(MyActivity.this); //context of class wrapped in
            try {
                addressList = geocoder.getFromLocationName(placeName,1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return addressList;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            long threadId = Thread.currentThread().getId();
            Log.d(_logTag,"onPostExecute threadID: "+threadId);
            MyActivity.this.clearDisplay();
            for(Address address: addresses){
                MyActivity.this.displayAddressLines(address);
            }
            if(addresses.size()==0){
                MyActivity.this.clearDisplay();
                MyActivity.this.addLineToDisplay("I'm sorry, we cannot find your location");
            }
        }
    }

    class GetAddressesFromLocation extends AsyncTask<Location, Void, List<Address>>{

        @Override
        protected List<Address> doInBackground(Location... params) {
            long threadId = Thread.currentThread().getId();
            Log.d(_logTag,"doInBackground Thread ID: "+threadId);
            Location location = params[0];
            List<Address> addressList = null;

            Geocoder geocoder = new Geocoder(MyActivity.this); //context of class wrapped in
            try {
                addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),5);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return addressList;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            long threadId = Thread.currentThread().getId();
            Log.d(_logTag,"onPostExecute threadID: "+threadId);
            MyActivity.this.clearDisplay();
            for(Address address: addresses){
                MyActivity.this.displayAddressLines(address);
            }
        }
    }




}