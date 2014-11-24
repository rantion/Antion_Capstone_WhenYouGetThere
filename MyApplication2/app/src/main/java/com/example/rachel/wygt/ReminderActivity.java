package com.example.rachel.wygt;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by Rachel on 10/23/14.
 */
public class ReminderActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_reminder);
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String reminder = extras.getString("Reminder");
            long idNum = (Long)extras.get("id");
            TextView textView = (TextView)findViewById(R.id.display_reminder);
            if(textView!=null){
                textView.setText(reminder);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return true;
    }
}
