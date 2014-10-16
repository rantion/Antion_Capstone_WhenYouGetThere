package com.example.rachel.wygt;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by Rachel on 10/15/14.
 */
public class CreateTaskActivity extends Activity{

    private LatLng destinationLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_task);
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            destinationLocation = (LatLng)extras.get("Destination_location");
            String destination = (String) extras.get("Destination");
            TextView _destination = (TextView)findViewById(R.id.destination_address);
            if(_destination!=null) {
                _destination.setText(destination);
            }
        }
    }


}
