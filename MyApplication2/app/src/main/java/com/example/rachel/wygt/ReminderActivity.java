package com.example.rachel.wygt;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Rachel on 10/23/14.
 */
public class ReminderActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.reminder_layout);
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String reminder = extras.getString("Reminder");
            long idNum = (Long)extras.get("id");
            TextView textView = (TextView)findViewById(R.id.display_reminder);
            if(textView!=null){
                textView.setText(idNum+":  "+reminder);
            }
        }
    }
}
