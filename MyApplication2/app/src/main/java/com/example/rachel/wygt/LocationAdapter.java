package com.example.rachel.wygt;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Rachel on 11/11/14.
 */
public class LocationAdapter extends ArrayAdapter<MyLocation> {
    private final Context context;
    private final List<MyLocation> locations;
    private MyLocationDataSource locationDataSource = MyApplication.myLocationDataSource;

    public LocationAdapter(Context context, int resource, List<MyLocation> objects) {
        super(context, resource, objects);
        this.context = context;
        this.locations = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.location_item, parent, false);
        TextView name = (TextView)rowView.findViewById(R.id.location_name);
        TextView address = (TextView) rowView.findViewById(R.id.location_address);
        final MyLocation location = locations.get(position);
        LinearLayout crud = (LinearLayout) rowView.findViewById(R.id.location_item_layout);
        if (position % 2 == 0) {
            crud.setBackgroundColor(context.getResources().getColor(R.color.dark_purple));
        } else {
            crud.setBackgroundColor(context.getResources().getColor(R.color.lighter_purple));
        }

        name.setText(location.getName());
        String _address = location.getAddress();
        if(_address.length()>23) {
           String[]addressParts = _address.split("\n");
           String longAddress = "";
           for(String string: addressParts){
               longAddress = longAddress+string+", ";
           }
            if(longAddress.length()>40) {
                String shortenedAddress = longAddress.substring(0, 40);
                _address = shortenedAddress+"...";
            }
        }
        address.setText(_address);
        ImageView delete = (ImageView)rowView.findViewById(R.id.delete_location);
        delete.setTag(position);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TASKADAPTER", "OnClickSelected");
                locationDataSource.deleteMyLocation(location);
                locations.remove(location);
                notifyDataSetChanged();
            }
        });
        return rowView;
    }
}
