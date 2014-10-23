//package com.example.rachel.wygt;
//
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Toast;
//
///**
// * Cr
// * eated by Rachel on 10/20/14.
// */
//public class ProximityIntentReceiver extends BroadcastReceiver {
//
//    private String LogTag = "ProximityIntentReciever";
////    private GPSTracker gpsTracker;
//    private LocationManager lm;
//
//    private static final int NOTIFICATION_ID = 1000;
//
//    public ProximityIntentReceiver(){
////        gpsTracker = new GPSTracker(MyApplication.getAppContext());
////        lm = gpsTracker.getLocationManager();
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String info = "";
//        Toast.makeText(MyApplication.getAppContext(),
//                "Proximity Alert Recieved!", Toast.LENGTH_SHORT)
//                .show();
//        String key = LocationManager.KEY_PROXIMITY_ENTERING;
//        Boolean entering = intent.getBooleanExtra(key, false);
//
//        if (entering) {
//            Log.d(LogTag, "entering");
//        } else {
//            Log.d(LogTag, "exiting");
//        }
////        Bundle extras = intent.getExtras();
////        if(extras!=null){
////            long time = (Long)extras.get("Time");
////            String destination = (String)extras.get("Destination");
////            info = "Set at: "+time+" For: "+destination+".\n";
////        }
//        NotificationManager notificationManager =
//                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,intent, 0);
//        Notification notification = createNotification();
//
//        notification.setLatestEventInfo(context,
//                "Proximity Alert!", info, pendingIntent);
//        notificationManager.notify(NOTIFICATION_ID, notification);
////        lm.removeProximityAlert(pendingIntent);
//    }
//
//    private Notification createNotification() {
//        Notification notification = new Notification();
//        notification.icon = R.drawable.ic_launcher;
//        notification.when = System.currentTimeMillis();
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
//        notification.defaults |= Notification.DEFAULT_VIBRATE;
//        notification.defaults |= Notification.DEFAULT_LIGHTS;
//        notification.ledARGB = Color.RED;
//        notification.ledOnMS = 1500;
//        notification.ledOffMS = 1500;
//        return notification;
//    }
//
//}
//
//
