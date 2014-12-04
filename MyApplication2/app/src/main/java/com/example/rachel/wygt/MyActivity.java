package com.example.rachel.wygt;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class MyActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, OnKeyListener {

    String LOGTAG = "*** WYGT MainActivity ***";
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationClient locationClient;
    LocationRequest mLocationRequest;
    boolean mUpdatesRequested;
    private AlarmManager alarmManager;
    private Intent gpsTrackerIntent;
    private PendingIntent pendingIntent;
    private Location mLocation;
    private GoogleMap googleMap;
    private Marker _marker;
    double latitude; // latitude
    double longitude; // longitude
    private GPSTracker gpsTracker;
    private int intervalInMinutes = 1;
    private boolean currentlyTracking, usingCustLocation;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    private LocationManager lm;
    Location location; // location
    private SimpleAdapter adapter;
    private CallNotificationReceiver callReciever = new CallNotificationReceiver();
    private String destinationDuration, destinationDistance, destDistMeters, destDistSeconds;
    private String destinationAddress, destinationName;
    private LatLng destinationLocation;
    private PreferenceChangeListener preferenceListener = new PreferenceChangeListener();
    private MyLocationDataSource locationDataSouce = MyApplication.getMyLocationDataSource();
    private List<MyLocation> myLocations = locationDataSouce.getAllMyLocations();
    private ArrayList<Map<String, String>> _myLocations;
    private ImageView star;
    MyLocation custLoc;
    private AutoWithSpaces enterLocation;
    private ProgressDialog progress, dialog;
    private LatLng destinationLoc;

    private TextWatcher textWatch = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (enterLocation.getText().toString().length() == 0) {
                LinearLayout mapLayout = (LinearLayout) findViewById(R.id.map_layout);
                LinearLayout mapFrag = (LinearLayout) findViewById(R.id.map_fragment);
                LinearLayout navBar = (LinearLayout) findViewById(R.id.destination_bar);
                if (navBar != null) {
                    navBar.setVisibility(View.VISIBLE);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mapFrag.getLayoutParams();
                    params.weight = 100;
                    mapLayout.updateViewLayout(mapFrag, params);
                }
                usingCustLocation = false;
                progress.dismiss();
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        usingCustLocation = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        int notificationHeight = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int notHeight = 0;
        if (notificationHeight > 0) {
            notHeight = getResources().getDimensionPixelSize(notificationHeight);
        }
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        float density  = getResources().getDisplayMetrics().density;
        float dpHeight = (outMetrics.heightPixels-mActionBarSize-notHeight) / density;
        float dpWidth  = outMetrics.widthPixels / density;
        MyApplication.setHeight(dpHeight);
        MyApplication.setWidth(dpWidth);
        MyApplication.setActivityContext(this);
        enterLocation = (AutoWithSpaces) findViewById(R.id.enter_location_field);
        enterLocation.addTextChangedListener(textWatch);
        gpsTracker = new GPSTracker();
        gpsTracker.registerReceivers();
        progress = new ProgressDialog(MyActivity.this);
        progress.setMessage("Calculating...");
        progress.setIndeterminate(false);
        progress.setCancelable(true);

        MyApplication.setGpsTracker(gpsTracker);
        star = (ImageView) findViewById(R.id.save_location_star);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        confirmNetworkProviderAvailable(lm);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener);
        servicesConnected();
        EditText addressBox = (EditText) findViewById(R.id.enter_location_field);
        addressBox.setOnKeyListener(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean firstTimeLoadindApp = sharedPreferences.getBoolean("firstTimeLoadindApp", true);
        if (firstTimeLoadindApp) {
            editor.putBoolean("firstTimeLoadindApp", false);
            editor.putBoolean("appIsOpen", true);
            editor.putString("appID", UUID.randomUUID().toString());
            editor.putString("prefSyncFrequency", "5");
            editor.putString("prefUsername", "username");
            editor.apply();
            startAlarmManager();
        }
        Log.d("GPS/APP IS OPEN", "appIsOpenSetToTrue - MyActivity");
        editor.putBoolean("appIsOpen", true);
        editor.apply();
        lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0, MyApplication.getGpsTracker());
        initializeMap();
        lm.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0, MyApplication.getGpsTracker());
        lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, MyApplication.getGpsTracker());
        boolean alarmUp = (PendingIntent.getBroadcast(MyApplication.getAppContext(), MyApplication.REQUESTCODE,
                new Intent("com.example.wygt.alarm"),
                PendingIntent.FLAG_NO_CREATE) != null);
        Log.d(LOGTAG, "wtf man");
        if (alarmUp = false) {
            startAlarmManager();
        } else {
            Log.d(LOGTAG, "Alarm is running");
        }
        populateLocations();
        adapter = new SimpleAdapter(MyApplication.getAppContext(), _myLocations, R.layout.custlocview,
                new String[]{"Name", "Address"}, new int[]{
                R.id.clocName, R.id.cLocAddress}
        );
        final AutoCompleteTextView enter = (AutoCompleteTextView) findViewById(R.id.enter_location_field);
        enter.setThreshold(1);
        enter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> _loc = (Map<String, String>) parent
                        .getItemAtPosition(position);
                String index = _loc.get("Index");
                MyLocation loc = myLocations.get(Integer.parseInt(index));
                String current = loc.getName();
                enter.setText(current);
                enter.setSelection(current.length());
                usingCustLocation = true;
                custLoc = loc;
                destinationName = loc.getName();
                destinationLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
                destinationAddress = loc.getAddress();
                getAddress(enter);
                adapter.notifyDataSetChanged();
                getAdapter();
                enter.setAdapter(adapter);
            }
        });
        enter.setAdapter(adapter);
    }

    public void getAdapter() {
        populateLocations();
        adapter = new SimpleAdapter(MyApplication.getAppContext(), _myLocations, R.layout.custlocview,
                new String[]{"Name", "Address"}, new int[]{
                R.id.clocName, R.id.cLocAddress}
        );
    }

    public void populateLocations() {
        _myLocations = new ArrayList<Map<String, String>>();
        myLocations = locationDataSouce.getAllMyLocations();
        for (int i = 0; i < myLocations.size(); i++) {
            Map<String, String> LocationAddress = new HashMap<String, String>();
            LocationAddress.put("Name", myLocations.get(i).getName());
            LocationAddress.put("Address", myLocations.get(i).getAddress());
            LocationAddress.put("Index", String.valueOf(i));
            _myLocations.add(LocationAddress);
        }
    }

    public void starSaveLocation(View view) {
        if (usingCustLocation) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("are you sure you want to delete location?")
                    .setCancelable(true)
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    })
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            locationDataSouce.deleteMyLocation(custLoc);
                            Toast.makeText(MyApplication.getAppContext(), "Location Deleted", Toast.LENGTH_SHORT).show();
                            star.setImageDrawable(getResources().getDrawable(R.drawable.starout));
                            TextView destName = (TextView) findViewById(R.id.cust_name);
                            destName.setVisibility(View.GONE);
                            destinationName = destinationAddress;
                            myLocations.remove(custLoc);
                            adapter.notifyDataSetChanged();
                            usingCustLocation = false;

                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();

        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText input = new EditText(this);
            builder.setView(input);
            builder.setMessage("please enter a location name")
                    .setCancelable(true)
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //        alert.dismiss();
                        }
                    })
                    .setPositiveButton("save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String name = input.getText().toString();
                           custLoc = locationDataSouce.createMyLocation(name, destinationAddress, destinationLocation.latitude, destinationLocation.longitude);
                            Toast.makeText(MyApplication.getAppContext(), "location saved!", Toast.LENGTH_SHORT).show();
                            star.setImageDrawable(getResources().getDrawable(R.drawable.star));
                            usingCustLocation = true;
                            destinationName = name;
                            TextView destName = (TextView) findViewById(R.id.cust_name);
                            destName.setVisibility(View.VISIBLE);
                            destName.setText(name);
                            myLocations.add(custLoc);
                            adapter.notifyDataSetChanged();

                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }


    @Override
    protected void onResume() {
        //   Toast.makeText(this,"OnResumeCalled",Toast.LENGTH_SHORT).show();
        myLocations = locationDataSouce.getAllMyLocations();
        mLocation = gpsTracker.getLocation();
        if (mLocation == null) {
            Log.d("Marker-RESUME", "mLocation == null");
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Log.d("MYACTIVITY", "back key pressed");
        moveTaskToBack(true);
        MyActivity.this.finish();
    }

    private void startAlarmManager() {
        Log.d("GPS-MyActivity", "startAlarmManager");
        Context context = getBaseContext();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        gpsTrackerIntent = new Intent("com.example.wygt.alarm");
        gpsTrackerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pendingIntent = PendingIntent.getBroadcast(context, MyApplication.REQUESTCODE, gpsTrackerIntent, 0);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String interval = sharedPreferences.getString("prefSyncFrequency", "5");
        intervalInMinutes = Integer.parseInt(interval);
        Log.d("GPS-MyActivity", "Shared Preferences interval: " + intervalInMinutes);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                intervalInMinutes * 60000, // 60000 = 1 minute
                pendingIntent);
    }

    private void cancelAlarmManager() {
        Log.d(LOGTAG, "cancelAlarmManager");
        Context context = getBaseContext();
        Intent intent = new Intent("com.example.wygt.alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MyApplication.REQUESTCODE, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void getAddress(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        Log.d(LOGTAG, "getAddress clicked");
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.JELLY_BEAN
                &&
                Geocoder.isPresent()) {
            progress.show();
        //    confirmWifiAvailable();
            AutoCompleteTextView _location = (AutoCompleteTextView) findViewById(R.id.enter_location_field);
            String locationName = _location.getText().toString();
            if (locationName.length() > 0) {
                if (usingCustLocation) {
                    mLocation = gpsTracker.getLocation();
                    final LatLng currentLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                    if (_marker != null) {
                        _marker.remove();
                    }
                    try {
                        (new GetDrivingDistance()).execute(destinationLocation, currentLocation).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(destinationLocation).title(destinationName);
                    _marker = googleMap.addMarker(marker);
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            destinationLocation).zoom(14).build();
                    _marker.setSnippet(destinationAddress);
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    LinearLayout mapLayout = (LinearLayout) findViewById(R.id.map_layout);
                    float weightSum = mapLayout.getWeightSum();
                    LinearLayout mapFrag = (LinearLayout) findViewById(R.id.map_fragment);
                    LinearLayout navBar = (LinearLayout) findViewById(R.id.destination_bar);
                    if (navBar != null) {
                        navBar.setVisibility(View.VISIBLE);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mapFrag.getLayoutParams();
                        params.weight = (float) (weightSum * .73);
                        mapLayout.updateViewLayout(mapFrag, params);
                        TextView destName = (TextView) findViewById(R.id.cust_name);
                        destName.setVisibility(View.VISIBLE);
                        destName.setText(destinationName);
                        TextView destAddress = (TextView) findViewById(R.id.activity_my_destination);
                        if (destAddress != null) {
                            destAddress.setText(destinationAddress);
                        }
                    }
                    star.setImageDrawable(getResources().getDrawable(R.drawable.star));
                    ImageButton doSomethingButton = (ImageButton) findViewById(R.id.do_something_button);

                    doSomethingButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MyActivity.this, DoSomethingActivity.class);
                            intent.putExtra("Destination", destinationName);
                            intent.putExtra("Destination_location", destinationLocation);
                            intent.putExtra("Current_Location", currentLocation);
                            intent.putExtra("Distance", destinationDistance);
                            intent.putExtra("Duration", destinationDuration);
                            intent.putExtra("DistanceMeters", destDistMeters);
                            intent.putExtra("DurationSeconds", destDistSeconds);
                            startActivity(intent);
                        }
                    });

                } else {
                    (new GetAddressesFromName()).execute(locationName);
                }
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
                progress.dismiss();
                return;
            }
        }
    }

    private void initializeMap() {
        Log.d("Marker", "Initialize Map Called");
        if (googleMap == null) {
            Log.d("Marker", "Map is Null");
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            Location current = gpsTracker.getLocation();
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(final Marker marker) {
                    destinationLoc = marker.getPosition();
                    if (_marker != null) {
                        _marker.remove();
                    }
                    destinationLocation = destinationLoc;
                    final LatLng currentLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                    final LatLng destination = marker.getPosition();

                    new GetAddressesFromLocation().execute(marker.getPosition());

                    return true;
                }
            });
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (_marker != null) {
                        _marker.remove();
                    }
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    //         (new GetAddressesFromLocation()).execute(latLng);

                    MarkerOptions marker = new MarkerOptions();
                    marker.position(latLng);
                    _marker = googleMap.addMarker(marker);
                }


            });

            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    if (_marker != null) {
                        marker.remove();

                    }
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));
                    MarkerOptions Mmarker = new MarkerOptions();
                    Mmarker.position(marker.getPosition());
                    _marker = googleMap.addMarker(Mmarker);
                    _marker.setDraggable(true);
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            marker.getPosition()).zoom(14).build();
                }
            });
            if (current == null)

            {
                Log.d("Marker", "CurrentIsNull");
                confirmNetworkProviderEnabled(lm);
               // confirmWifiAvailable();

            }

            if (current != null)

            {
                Log.d("Marker", "current is not null");
                LatLng currentPos = new LatLng(current.getLatitude(), current.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 14));
                MarkerOptions marker = new MarkerOptions();
                LatLng currentLoc = new LatLng(current.getLatitude(), current.getLongitude());
                marker.position(currentLoc).title("Current Location");
                _marker = googleMap.addMarker(marker);
                _marker.setDraggable(true);
                CameraPosition cameraPosition = new CameraPosition.Builder().target(
                        currentLoc).zoom(14).build();
            }
            // check if map is created successfully or not
            if (googleMap == null)

            {
                Toast.makeText(this,
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_settings) {
            Intent intent = new Intent(this, UserSettingsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.list) {
            Intent intent = new Intent(this, TaskListActivity.class);
            startActivity(intent);
        }
        if (id == R.id.listLocations) {
            Intent intent = new Intent(this, MyLocationsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("GPS/APP IS OPEN", "MyActivityOnDestroyCalled - appIsOpen set to false");
        editor.putBoolean("appIsOpen", false);
        editor.apply();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
//        SharedPreferences sharedPreferences = PreferenceManager
//                .getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        Log.d("GPS/APP IS OPEN", "MyActivityOnStopCalled - appIsOpen set to false");
//        editor.putBoolean("appIsOpen", false);
//        editor.apply();
        super.onStop();
    }

    @Override
    protected void onPause() {
//       SharedPreferences sharedPreferences = PreferenceManager
//                .getDefaultSharedPreferences(MyApplication.getAppContext());
//        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("GPS/APP IS OPEN", "onPause_MyActivityCalled... no change to appIsOpen");
//        editor.putBoolean("appIsOpen", false);
//        editor.apply();
        super.onPause();
    }

    boolean confirmNetworkProviderAvailable(LocationManager lm) {
        boolean networkAvailable = confirmAirplaneModeIsOff() &&
                confirmNetworkProviderEnabled(lm);
        return networkAvailable;
    }

    public boolean confirmNetworkProviderEnabled(LocationManager lm) {
        boolean isAvailable = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        isAvailable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isAvailable) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("please enable location services");
            builder.setMessage("wygt only works if it knows where you are!")
                    .setCancelable(true)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(viewIntent);
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
//
//            AlertUserDialog dialog = new AlertUserDialog("please enable location services jkjh", Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            dialog.show(getFragmentManager(), null);

            isAvailable = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            isAvailable = lm
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        return isAvailable;

    }


    public boolean confirmAirplaneModeIsOff() {

        Log.d(LOGTAG, "inside AirPlaneMode");
        boolean isOff =
                Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 0;
        Log.d(LOGTAG, "AirPlane Mode Is : " + isOff);
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
        isAvailable = wifiInfo.isConnectedOrConnecting();
        if (!isAvailable) {
            AlertUserDialog dialog = new AlertUserDialog("Please Enable Your WiFi", Settings.ACTION_WIFI_SETTINGS);
            dialog.show(getFragmentManager(), null);
            isAvailable = wifiInfo.isAvailable();
          //  isAvailable = wifiInfo.isConnectedOrConnecting();
        }
        return isAvailable;
    }

    @Override
    public void recreate() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    MyActivity.this.finish();
                    MyActivity.this.startActivity(MyActivity.this.getIntent());
                } else MyActivity.this.recreate();
            }
        }, 1);
        super.recreate();
    }

    //    private void clearDisplay() {
////        TextView textView = (TextView)findViewById(R.id.textView);
////        textView.setText("");
//    }

//    private void displayAddressLines(Address address) {
//        int lastIndex = address.getMaxAddressLineIndex();
//        for (int i = 0; i <= lastIndex; i++) {
//            String addressLine = address.getAddressLine(i);
//            addLineToDisplay(addressLine);
//        }
//        addLineToDisplay("");
//    }

//    private void addLineToDisplay(CharSequence displayLine) {
////        TextView textView = (TextView)findViewById(R.id.textView);
////
////        CharSequence existingText = textView.getText();
////        CharSequence newText = existingText + "\n"+ displayLine;
////
////        textView.setText(newText);
//
//    }


    public String getDestinationDistance() {
        return destinationDistance;
    }

    public void setDestinationDistance(String destinationDistance) {
        this.destinationDistance = destinationDistance;
    }

    public String getDestinationDuration() {
        return destinationDuration;
    }

    public void setDestinationDuration(String destinationDuration) {
        this.destinationDuration = destinationDuration;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
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
            Log.d(LOGTAG, String.valueOf(connectionResult.getErrorCode()));
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
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

//        if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
//                keyCode == EditorInfo.IME_ACTION_DONE ||
//                event.getAction() == KeyEvent.ACTION_DOWN &&
//                        event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//
//            if (!event.isShiftPressed()) {
//                Log.v("AndroidEnterKeyActivity", "Enter Key Pressed!");
//                switch (view.getId()) {
//                    case R.id.enter_location_field:
//                        mActivityIndicator.setVisibility(View.GONE);
//                        LinearLayout navBar = (LinearLayout) findViewById(R.id.destination_bar);
//                        navBar.setVisibility(View.GONE);
//                        break;
//                }
//                return true;
//            }
//
//        }
        return false;
    }


    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST:

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


    private class GetAddressesFromName extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... params) {
            long threadId = Thread.currentThread().getId();
            Log.d(LOGTAG, "doInBackground Thread ID: " + threadId);
            String placeName = params[0];
            List<Address> addressList = null;

            Geocoder geocoder = new Geocoder(MyActivity.this, Locale.US); //context of class wrapped in
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyActivity.this);
                    builder.setTitle("Select your address: ");

                    builder.setItems(_addresses, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            Address selectedAddress = addressArray[which];
                            mLocation = gpsTracker.getLocation();
                            final LatLng currentLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                            destinationLocation = new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude());
                            if (_marker != null) {
                                _marker.remove();
                            }
                            try {
                                (new GetDrivingDistance()).execute(destinationLocation, currentLocation).get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            destinationAddress = (String) _addresses[which];
                            MarkerOptions marker = new MarkerOptions();
                            marker.position(destinationLocation).title(selectedAddress.getAddressLine(0));
                            _marker = googleMap.addMarker(marker);
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                                    destinationLocation).zoom(14).build();
                            _marker.setSnippet((String) _addresses[which]);
                            _marker.setDraggable(true);
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            LinearLayout mapLayout = (LinearLayout) findViewById(R.id.map_layout);
                            float weightSum = mapLayout.getWeightSum();
                            LinearLayout mapFrag = (LinearLayout) findViewById(R.id.map_fragment);
                            LinearLayout navBar = (LinearLayout) findViewById(R.id.destination_bar);
                            if (navBar != null) {
                                navBar.setVisibility(View.VISIBLE);
                                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mapFrag.getLayoutParams();
                                params.weight = (float) (weightSum * .73);
                                mapLayout.updateViewLayout(mapFrag, params);
                                TextView destAddress = (TextView) findViewById(R.id.activity_my_destination);
                                if (destAddress != null) {
                                    destAddress.setText(_addresses[which]);
                                }
                            }
                            progress.dismiss();
                            ImageButton doSomethingButton = (ImageButton) findViewById(R.id.do_something_button);

                            doSomethingButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(MyActivity.this, DoSomethingActivity.class);
                                    intent.putExtra("Destination", _addresses[which]);
                                    intent.putExtra("Destination_location", destinationLocation);
                                    intent.putExtra("Current_Location", currentLocation);
                                    intent.putExtra("Button", "Do Something");
                                    intent.putExtra("Distance", destinationDistance);
                                    intent.putExtra("Duration", destinationDuration);
                                    intent.putExtra("DistanceMeters", destDistMeters);
                                    intent.putExtra("DurationSeconds", destDistSeconds);
                                    startActivity(intent);
                                }
                            });


                        }
                    });
                    builder.show();
                }
            }
        }

//        GoogleMap.OnMarkerClickListener MarkerListener = new GoogleMap.OnMarkerClickListener() {
//
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                Intent intent = new Intent(MyActivity.this, CreateTaskActivity.class);
//                intent.putExtra("Destination", marker.getSnippet());
//                intent.putExtra("Destination_location", marker.getPosition());
//                startActivity(intent);
//                return false;
//            }
//        };
    }


    class GetAddressesFromLocation extends AsyncTask<LatLng, Void, List<Address>> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MyActivity.this);
            dialog.setMessage("Calculating...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);
            dialog.show();

        }

        @Override
        protected List<Address> doInBackground(LatLng... params) {

            long threadId = Thread.currentThread().getId();
            Log.d(LOGTAG, "doInBackground Thread ID: " + threadId);
            LatLng location = params[0];
            List<Address> addressList = null;
            Geocoder geocoder = new Geocoder(MyActivity.this); //context of class wrapped in
            try {
                addressList = geocoder.getFromLocation(location.latitude, location.longitude, 2);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return addressList;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            int j = 0;
            final CharSequence[] _addresses = new CharSequence[addresses.size()];
            final Address[] addressArray = new Address[addresses.size()];
            Location location1 = gpsTracker.getLocation();
            LatLng current = new LatLng(location1.getLatitude(), location1.getLongitude());
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
            String address = (String) _addresses[0];
            try {
                new GetDrivingDistance().execute(current, destinationLoc).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            _marker.setTitle(address);
            destinationAddress = address;
            long threadId = Thread.currentThread().getId();
            Log.d(LOGTAG, "onPostExecute threadID: " + threadId);
            MarkerOptions Mmarker = new MarkerOptions();
            Mmarker.position(destinationLoc).title(destinationAddress);
            _marker = googleMap.addMarker(Mmarker);
            _marker.setDraggable(true);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    destinationLoc).zoom(16).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            LinearLayout mapLayout = (LinearLayout) findViewById(R.id.map_layout);
            float weightSum = mapLayout.getWeightSum();
            LinearLayout mapFrag = (LinearLayout) findViewById(R.id.map_fragment);
            LinearLayout navBar = (LinearLayout) findViewById(R.id.destination_bar);
            if (navBar != null) {
                navBar.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mapFrag.getLayoutParams();
                params.weight = (float) (weightSum * .73);
                mapLayout.updateViewLayout(mapFrag, params);
                TextView destAddress = (TextView) findViewById(R.id.activity_my_destination);
                if (destAddress != null) {
                    destAddress.setText(destinationAddress);
                }
            }

            star.setImageDrawable(getResources().getDrawable(R.drawable.starout));
            ImageButton doSomethingButton = (ImageButton) findViewById(R.id.do_something_button);
            dialog.dismiss();
            doSomethingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Location current = gpsTracker.getLocation();
                    LatLng currentLocation = new LatLng(current.getLatitude(), current.getLongitude());
                    Intent intent = new Intent(MyActivity.this, DoSomethingActivity.class);
                    intent.putExtra("Destination", destinationAddress);
                    intent.putExtra("Destination_location", destinationLoc);
                    intent.putExtra("Current_Location", currentLocation);
                    intent.putExtra("Button", "Do Something");
                    intent.putExtra("Distance", destinationDistance);
                    intent.putExtra("Duration", destinationDuration);
                    intent.putExtra("DistanceMeters", destDistMeters);
                    intent.putExtra("DurationSeconds", destDistSeconds);
                    startActivity(intent);
                }
            });
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
            //   pDialog.dismiss();
            progress.dismiss();
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
                            MyActivity.this.destDistSeconds = _duration.getString("value");
                            JSONObject distance = element.getJSONObject("distance");

                            MyActivity.this.destDistMeters = distance.getString("value");
                            distances.add(distance.getString("text"));
                        }
                        String _distances = null;
                        for (String distance : distances) {
                            _distances = distance;
                        }
                        TextView _duration = (TextView) findViewById(R.id.duration_approximation);
                        TextView _distance = (TextView) findViewById(R.id.distance_approximation);
                        String distance = _distances;
                        _distance.setText(distance);
                        _duration.setText("ETA: " + duration);
                        MyActivity.this.setDestinationDistance(distance);
                        MyActivity.this.setDestinationDuration(duration);


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}