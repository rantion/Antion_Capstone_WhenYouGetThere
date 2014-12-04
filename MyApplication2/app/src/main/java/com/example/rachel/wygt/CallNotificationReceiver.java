package com.example.rachel.wygt;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Rachel on 11/4/14.
 */
public class CallNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("onReciveCalled", "inside");
        Bundle extras = intent.getExtras();
        if(extras!= null){
            Intent _intent = new Intent(Intent.ACTION_CALL);
            Uri data = (Uri)extras.get("URI");
            _intent.setData(data);
            _intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(_intent);
            NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int id = (Integer)extras.get("ID");
            nMgr.cancel(id);
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }


    }
}
