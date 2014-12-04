package com.example.rachel.wygt;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Rachel on 11/4/14.
 */
public class TaskAdapter extends ArrayAdapter<Task> {
    private final Context context;
    private final List<Task> tasks;
    private TaskDataSource tds = MyApplication.getTaskDataSource();
    private TaskSoundDataSource tsds = MyApplication.getTaskSoundDataSource();
    private TaskContactDataSource tcds = MyApplication.getTaskContactDataSource();

    public TaskAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        this.context = context;
        this.tasks = objects;


    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.task_reminder_item, parent, false);
        TextView location = (TextView) rowView.findViewById(R.id.task_item_location);
        TextView radius = (TextView) rowView.findViewById(R.id.task_item_radius);
        TextView message = (TextView) rowView.findViewById(R.id.task_item_message);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.task_item_icon);
        final Task task = tasks.get(position);
        String[] addressL = task.getAddress().split("\n");
        String address = "";
        if(addressL.length>1) {
            address = addressL[0] + "\n" + addressL[1];
        }
        else{
            address = addressL[0];
        }
        location.setText(address);
        String radiusString = "there";
        Calendar cl = Calendar.getInstance();
        cl.setTimeInMillis(task.getTime());  //here your time in miliseconds
        String date = "" + cl.get(Calendar.DAY_OF_MONTH) + "/" + (cl.get(Calendar.MONTH)+1) + "/" + cl.get(Calendar.YEAR);
        String time = "" + cl.get(Calendar.HOUR) + ":" + cl.get(Calendar.MINUTE);

        if (!task.getRadius_type().equals("there")) {
            radiusString = task.getOriginalRadius() + " " + task.getRadius_type();
        }
        radius.setText(time + " " + date + "\n" + radiusString);
        Drawable icon = null;
        LinearLayout crud = (LinearLayout) rowView.findViewById(R.id.crud_layout);
        if (position % 2 == 0) {
            crud.setBackgroundColor(context.getResources().getColor(R.color.dark_purple));
        } else {
            crud.setBackgroundColor(context.getResources().getColor(R.color.lighter_purple));
        }
        if (task.getTaskType() == Task.CALL_REMINDER_TASK_TYPE) {
            icon = MyApplication.getAppContext().getResources().getDrawable(R.drawable.phone);
            imageView.setImageDrawable(icon);
            List<TaskContact> contacts = tcds.getTaskContactsByTaskId(task.getId());
            message.setText("Call: " + contacts.get(0).getName());
        } else if (task.getTaskType() == Task.TEXT_MESSAGE_TASK_TYPE) {
            icon = MyApplication.getAppContext().getResources().getDrawable(R.drawable.text);
            imageView.setImageDrawable(icon);
            if (task.getReminder().length() > 30) {
                message.setText(task.getReminder().substring(0, 30)+"...");
            } else {
                message.setText(task.getReminder());
            }
        } else if (task.getTaskType() == Task.REMINDER_MESSAGE_TASK_TYPE) {
            icon = MyApplication.getAppContext().getResources().getDrawable(R.drawable.reminder);
            imageView.setImageDrawable(icon);
            if (task.getReminder().length() > 30) {
                message.setText(task.getReminder().substring(0, 30)+"...");
            } else {
                message.setText(task.getReminder());
            }
        } else if (task.getTaskType() == Task.SOUND_SETTING_TASK_TYPE) {
            icon = MyApplication.getAppContext().getResources().getDrawable(R.drawable.audio);
            imageView.setImageDrawable(icon);
            message.setText("Sound Settings");
        }
        ImageView delete = (ImageView)rowView.findViewById(R.id.delete_task_from_layout);
        delete.setTag(position);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Log.d("TASKADAPTER", "OnClickSelected");
              if(task.getTaskType()==Task.SOUND_SETTING_TASK_TYPE){
                  List<SoundSettings> sounds = tsds.getTaskSoundsByTaskId(task.getId());
                  for(SoundSettings sound: sounds){
                      tsds.deleteTaskSound(sound);
                  }
              }
              if(task.getTaskType() == Task.TEXT_MESSAGE_TASK_TYPE || task.getTaskType() == Task.CALL_REMINDER_TASK_TYPE){
                  List<TaskContact> contacts = tcds.getTaskContactsByTaskId(task.getId());
                  for(TaskContact contact: contacts){
                      tcds.deleteTaskContact(contact);
                  }
              }
              tds.deleteTask(task);
              tasks.remove(task);
              notifyDataSetChanged();
            }
        });
        return rowView;
    }
}
