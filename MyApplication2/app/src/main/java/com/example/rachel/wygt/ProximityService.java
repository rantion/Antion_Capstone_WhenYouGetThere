//package com.example.rachel.wygt;
//
//import android.app.IntentService;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.database.Cursor;
//import android.graphics.Color;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.util.Log;
//import android.widget.Toast;
//
//
//public class ProximityService extends Service {
//
//    private static final String PROX_ALERT_INTENT = "Proximity_Alert_Intent";
//    int n = 0;
//    private BroadcastReceiver mybroadcast;
//    private GPSTracker gpsTracker;
//
//
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public void onCreate() {
//        Log.d("ProximityService", "Proximity Service Created");
//        mybroadcast = new ProximityAlertIntentReceiver();
//        gpsTracker = MyApplication.getGpsTracker();
//
//    }
//
//    @Override
//    public void onDestroy() {
//        Log.d("Proximity Service: ", "Proximity Service Stopped");
//        try {
//            unregisterReceiver(mybroadcast);
//        } catch (IllegalArgumentException e) {
//            Log.d("reciever", e.toString());
//        }
//
//
//    }
//
////    @Override
////    public int onStartCommand(Intent intent, int flags, int startId) {
////      return START_STICKY;
////    }
//
//    @Override
//    public void onStart(Intent intent, int startid) {
//        Log.d("Proximity Service", "ProximityService Started!");
//        IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
//        registerReceiver(mybroadcast, filter);
//
//
//    }
//
//    public static class ProximityAlertIntentReceiver extends BroadcastReceiver {
//        private String LogTag = "ProximityIntentReciever";
//        private static final int NOTIFICATION_ID = 1000;
//
//        public ProximityAlertIntentReceiver(){
//
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String info = "";
//            Toast.makeText(MyApplication.getAppContext(),
//                    "Proximity Alert Recieved!", Toast.LENGTH_SHORT)
//                    .show();
//            String key = LocationManager.KEY_PROXIMITY_ENTERING;
//            Boolean entering = intent.getBooleanExtra(key, false);
//
//            if (entering) {
//                Log.d(LogTag, "entering");
//            } else {
//                Log.d(LogTag, "exiting");
//            }
//
//            Bundle extras = intent.getExtras();
//            if (extras != null) {
//                long time = (Long) extras.get("Time");
//                String destination = (String) extras.get("Destination");
//                info = "Set at: " + time + " For: " + destination + ".\n";
//            }
//            NotificationManager notificationManager =
//                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//            Notification notification = createNotification();
//
//            notification.setLatestEventInfo(context,
//                    "YO NOTIFICATION IS NOTIFYING YOU",info, pendingIntent);
//            notificationManager.notify(NOTIFICATION_ID, notification);
////        lm.removeProximityAlert(pendingIntent);
//
//        }
//
//        private Notification createNotification() {
//            Notification notification = new Notification();
//            notification.icon = R.drawable.ic_launcher;
//            notification.when = System.currentTimeMillis();
//            notification.flags |= Notification.FLAG_AUTO_CANCEL;
//            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
//            notification.defaults |= Notification.DEFAULT_VIBRATE;
//            notification.defaults |= Notification.DEFAULT_LIGHTS;
//            notification.ledARGB = Color.RED;
//            notification.ledOnMS = 1500;
//            notification.ledOffMS = 1500;
//            return notification;
//        }
//        //make actions
//
//
//    }
//
//}