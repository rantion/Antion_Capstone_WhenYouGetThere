package com.example.rachel.wygt;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.location.LocationClient;

import java.util.List;

/**
 * Created by Rachel on 10/17/14.
 */
public class GPSTracker extends Service implements LocationListener {

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 0; // 1 minute
    private static final int REMINDER_NOTIFICATION_ID = 1000;
    private final String LOGTAG = "GPS TRACKER";
    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";
    private TaskSoundDataSource taskSoundDataSource = MyApplication.getTaskSoundDataSource();
    private TaskContactDataSource taskContactDataSource = MyApplication.getTaskContactDataSource();
    private TaskDataSource taskDataSource = MyApplication.getTaskDataSource();
    protected LocationManager locationManager;
    Location location;
    double latitude;
    double longitude;
    private AudioManager audioManager = (AudioManager) MyApplication.getAppContext().getSystemService(Context.AUDIO_SERVICE);


    public GPSTracker() {
        Log.d(LOGTAG, "GPS Tracker created");
        getLocation();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        getLocation();
    }

    public void registerReceivers() {
        final Context context = MyApplication.getAppContext();

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                Bundle extras = arg1.getExtras();
                String message = "";
                String phoneNumber = "";
                if (extras != null) {
                    message = extras.getString("message");
                    phoneNumber = extras.getString("phoneNumber");
                }
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        Log.d(LOGTAG + "/SMS", "SMS sent to " + phoneNumber + " " + message);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));


        //---when the SMS has been delivered---
        MyApplication.getAppContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                Bundle extras = arg1.getExtras();
                String message = "";
                String phoneNumber = "";
                if (extras != null) {
                    message = extras.getString("message");
                    phoneNumber = extras.getString("phoneNumber");
                }
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        Log.d(LOGTAG + "/SMS", "SMS delivered");
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        sendSMS(phoneNumber, message);
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) MyApplication.getAppContext()
                    .getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {


//                AlertDialog.Builder alert = new AlertDialog.Builder(MyApplication.getAppContext());
//
//                alert.setTitle("please enable location services");
//
//
//                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        startActivity(intent);
//                    }
//                });
//                alert.show();
//
//
////                AlertUserDialog dialog = new AlertUserDialog("Please Enable Location Services", Settings.ACTION_LOCATION_SOURCE_SETTINGS);
////                Activity activity = (Activity) MyApplication.getActivityContext();
////                dialog.show(activity.getFragmentManager(), null);
            } else {
                Log.d("MARKER+GPS","canGetLocation");
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("Marker+ GPS Tracker", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.d("MARKER+GPS","gotNetworkLocation");
                        }
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                         Log.d(LOGTAG, "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.d("MARKER+GPS","gotGPSLocation");
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean isNull = location==null;
        if(isNull){
           location = getLastKnownLocation();
        }
        if(location == null){
            Log.d("MArker_GPS","location still null");
        }

        return location;
    }

    private Location getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    @Override
    public void onLocationChanged(Location location) {
        String provider = location.getProvider();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float accuracy = location.getAccuracy();
        long time = location.getTime();

        if(location != null){
            this.location = location;
            checkTextReminders();
            checkMessageReminders();
            checkCallReminders();
            checkSoundSettings();
        }


        String logMessage = LogHelper.FormatLocationInfo(provider, latitude, longitude, accuracy, time);
        Log.d(LOGTAG, "Monitor Location: " + logMessage);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        boolean appIsOpen = sharedPreferences.getBoolean("appIsOpen", true);

        if (!appIsOpen && location.getAccuracy() < 100f) {
            locationManager.removeUpdates(this);
            Log.d(LOGTAG, "locationManager removing updates");
        }
        boolean alarmUp = (PendingIntent.getBroadcast(MyApplication.getAppContext(), MyApplication.REQUESTCODE,
                new Intent("com.example.wygt.alarm"),
                PendingIntent.FLAG_NO_CREATE) != null);
        if (alarmUp = false) {
            startAlarmManager();
        } else {
     //       Log.d(LOGTAG, "Alarm is running");
        }
    }

    private void startAlarmManager() {
        Log.d("GPS-MyActivity", "startAlarmManager");
        Context context = getBaseContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent gpsTrackerIntent = new Intent("com.example.wygt.alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String interval = sharedPreferences.getString("prefSyncFrequency", "5");
        int intervalInMinutes = Integer.parseInt(interval);
        Log.d("GPS-MyActivity", "Shared Preferences interval: " + intervalInMinutes);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                intervalInMinutes * 60000, // 60000 = 1 minute
                pendingIntent);
    }

    private void checkSoundSettings() {
   //     Log.d(LOGTAG,"checking sound settings");
        List<Task> _tasks = taskDataSource.getSoundTasks();
  //      Log.d(LOGTAG+"/Sound",_tasks.size() +" tasks");
        for (Task task : _tasks) {
            float[] results = new float[4];
            long radius = task.getRadius();
            Location.distanceBetween(task.getLatitude(), task.getLongitude(), location.getLatitude(), location.getLongitude(), results);
            if (results[0] < (float) radius) {
                long taskId = task.getId();
                List<SoundSettings> sounds = taskSoundDataSource.getTaskSoundsByTaskId(taskId);
                for (SoundSettings sound : sounds) {
                    taskSoundDataSource.deleteTaskSound(sound);
                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, sound.getAlarm(), 0);
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, sound.getRinger(), 0);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sound.getMedia(), 0);
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, sound.getNotification(), 0);
                }
                taskDataSource.deleteTask(task);
            }
    //        Log.d(LOGTAG+"/Sound","task out of radius " + task.getLatitude() + "," + task.getLongitude() + "Radius: " + task.getRadius() + " distance: " + results[0]);
        }
    }

    private void checkCallReminders() {
  //      Log.d(LOGTAG,"checking call reminders");
        List<Task> _tasks = taskDataSource.getCallReminderTasks();
  //      Log.d(LOGTAG+"/Call",_tasks.size() +" tasks");
        for (Task task : _tasks) {
            float[] results = new float[4];
            long radius = task.getRadius();
            Location.distanceBetween(task.getLatitude(), task.getLongitude(), location.getLatitude(), location.getLongitude(), results);
            if (results[0] < (float) radius) {
                long taskId = task.getId();
                List<TaskContact> taskContacts = taskContactDataSource.getTaskContactsByTaskId(taskId);
                for (TaskContact con : taskContacts) {
                    Log.d(LOGTAG, "Calling " + con.getPhoneNumber());
                    Notification notification = createCallReminder(con.getName(), con.getPhoneNumber());
                    NotificationManager mNotificationManager = (NotificationManager) MyApplication.getAppContext().getSystemService(NOTIFICATION_SERVICE);
                    mNotificationManager.notify(1001, notification);
                    taskDataSource.deleteTask(task);
                    taskContactDataSource.deleteTaskContact(con);
                }
            }
  //          Log.d(LOGTAG+"/Call","task out of radius " + task.getLatitude() + "," + task.getLongitude() + "Radius: " + task.getRadius() + " distance: " + results[0]);
        }
    }

    private Notification createCallReminder(String name, String number) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, "Remember to Call " + name, when);
        RemoteViews contentView = new RemoteViews(MyApplication.getAppContext().getPackageName(), R.layout.call_notification);
        contentView.setTextViewText(R.id.call_reminder_textView, "Call " + name);
        notification.contentView = contentView;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
        notification.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
        notification.defaults |= Notification.DEFAULT_SOUND; // Sound
        notification.when = System.currentTimeMillis();
        notification.bigContentView = contentView;
        String uri = "tel:" + number.trim();
        Intent intent = new Intent("com.example.wygt.call");
        intent.putExtra("URI", Uri.parse(uri));
        intent.putExtra("ID", 1001);
      //  Intent intent = new Intent(Intent.ACTION_CALL);
     //   intent.setData(Uri.parse(uri));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MyApplication.getAppContext(), (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.call_reminder_call_button, pendingIntent);
        return notification;
    }

    private void checkTextReminders() {
   //     Log.d(LOGTAG + "/SMS", "checking Text Reminders");
        List<Task> tasks = taskDataSource.getTextTasks();
   //     Log.d(LOGTAG + "/SMS", tasks.size() + " tasks");
        for (Task task : tasks) {
            float[] results = new float[4];
            long radius = task.getRadius();
            Location.distanceBetween(task.getLatitude(), task.getLongitude(), location.getLatitude(), location.getLongitude(), results);
            if (results[0] < (float) radius) {
                long taskId = task.getId();
                List<TaskContact> taskContacts = taskContactDataSource.getTaskContactsByTaskId(taskId);
                for (TaskContact con : taskContacts) {
                    sendSMS(con.getPhoneNumber(), task.getReminder());
                    taskContactDataSource.deleteTaskContact(con);
                }
                taskDataSource.deleteTask(task);
            } else {
  //              Log.d("GPS TRACKER", "task out of radius " + task.getLatitude() + "," + task.getLongitude() + "Radius: " + task.getRadius() + " distance: " + results[0]);
            }
        }
    }

    private void sendSMS(final String phoneNumber, final String message) {
        Context context = MyApplication.getAppContext();
        Intent sentIntent = new Intent(SENT);
        sentIntent.putExtra("phoneNumber", phoneNumber);
        sentIntent.putExtra("message", message);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent deliveredIntent = new Intent(DELIVERED);
        deliveredIntent.putExtra("phoneNumber", phoneNumber);
        deliveredIntent.putExtra("message", message);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                deliveredIntent, PendingIntent.FLAG_ONE_SHOT);

        //---when the SMS has been sent---

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        Log.d(LOGTAG + "/SMS", "Sending " + message + " to " + phoneNumber);
    }

    private void checkMessageReminders() {
   //     Log.d(LOGTAG,"Checking message reminders");
        List<Task> values = taskDataSource.getReminderMessageTasks();
   //     Log.d(LOGTAG+"/Message",values.size()+ " tasks" );
        for (Task task : values) {
            float[] results = new float[4];
            long radius = task.getRadius();
            Location.distanceBetween(task.getLatitude(), task.getLongitude(), location.getLatitude(), location.getLongitude(), results);
            if (results[0] < (float) radius) {
                int requestID = (int) System.currentTimeMillis();
                Intent intent = new Intent(MyApplication.getAppContext(), ReminderActivity.class);
                intent.putExtra("Reminder", task.getReminder());
                intent.putExtra("id", task.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NotificationManager mNotificationManager = (NotificationManager) MyApplication.getAppContext().getSystemService(NOTIFICATION_SERVICE);
                Context context = MyApplication.getAppContext();
                PendingIntent pendingIntent = PendingIntent.getActivity(context, requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification notification = createNotification();
                notification.setLatestEventInfo(context,
                        "wygt reminder", null, pendingIntent);
                mNotificationManager.notify(REMINDER_NOTIFICATION_ID, notification);
                taskDataSource.deleteTask(task);
    //            Log.d("reminder", "you should see that shit");
            }
   //         Log.d(LOGTAG+"/Message","task out of radius " + task.getLatitude() + "," + task.getLongitude() + "Radius: " + task.getRadius() + " distance: " + results[0]);
        }
    }


    private Notification createNotification() {
        Notification notification = new Notification();
        notification.icon = R.drawable.ic_launcher;
        notification.when = System.currentTimeMillis();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.ledARGB = Color.RED;
        notification.ledOnMS = 1500;
        notification.ledOffMS = 1500;
        return notification;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
