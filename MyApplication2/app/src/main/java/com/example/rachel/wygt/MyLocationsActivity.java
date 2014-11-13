package com.example.rachel.wygt;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by Rachel on 11/11/14.
 */
public class MyLocationsActivity extends ListActivity {

    private List<MyLocation> myLocationList;
    private MyLocationDataSource locationDataSource= MyApplication.myLocationDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myLocationList = locationDataSource.getAllMyLocations();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LocationAdapter adapter = new LocationAdapter(this, R.layout.location_item, myLocationList);
        setListAdapter(adapter);
        getListView().setBackgroundColor(getResources().getColor(R.color.black));
//        View empty = getLayoutInflater().inflate(R.layout.no_tasks, null, false);
//        addContentView(empty, new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
//        getListView().setEmptyView(empty);
        super.onCreate(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_location_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.add_location:
                Intent intent = new Intent(this, AddLocationActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



}
