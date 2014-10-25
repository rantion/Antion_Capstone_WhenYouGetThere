package com.example.rachel.wygt;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Rachel on 10/24/14.
 */
public class UserSettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

}
