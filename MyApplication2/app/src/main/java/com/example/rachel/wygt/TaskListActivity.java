package com.example.rachel.wygt;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by Rachel on 11/4/14.
 */
public class TaskListActivity extends ListActivity {

    private TaskDataSource taskDataSource = MyApplication.getTaskDataSource();
    private TaskSoundDataSource taskSoundDataSource = MyApplication.getTaskSoundDataSource();
    private TaskContactDataSource taskContactDataSource = MyApplication.getTaskContactDataSource();
    private List<Task>tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tasks = taskDataSource.getAllTasks();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TaskAdapter adapter = new TaskAdapter(this,
                R.layout.task_reminder_item,tasks);
        setListAdapter(adapter);
        getListView().setBackgroundColor(getResources().getColor(R.color.black));
        View empty = getLayoutInflater().inflate(R.layout.no_tasks, null, false);
        addContentView(empty, new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
        getListView().setEmptyView(empty);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.d("TASKLISTACTIVITY", "ITEM SELECTED");
        final Task task = tasks.get(position);
        if(task.getTaskType() == Task.TEXT_MESSAGE_TASK_TYPE){
            List<TaskContact> contacts = taskContactDataSource.getTaskContactsByTaskId(task.getId());
            String contactInfo = "";
            for(TaskContact contact: contacts){
                contactInfo = contactInfo+ contact.getName()+" <"+contact.getPhoneNumber()+">, ";
            }
            Intent intent = new Intent(this,EditSMSTaskActivity.class);
            intent.putExtra("taskID", task.getId());
            intent.putExtra("lat", task.getLatitude());
            intent.putExtra("long", task.getLongitude());
            intent.putExtra("destination", task.getAddress());
            intent.putExtra("radiusType", task.getRadius_type());
            intent.putExtra("original", task.getOriginalRadius());
            intent.putExtra("message", task.getReminder());
            intent.putExtra("contacts",contactInfo);
            startActivity(intent);
        }
        if(task.getTaskType() == Task.CALL_REMINDER_TASK_TYPE){
            List<TaskContact> contacts = taskContactDataSource.getTaskContactsByTaskId(task.getId());
            String contactInfo = "";
            for(TaskContact contact: contacts){
                contactInfo = contactInfo+ contact.getName()+" <"+contact.getPhoneNumber()+">, ";
            }
            Intent intent = new Intent(this,EditCallTaskActivity.class);
            intent.putExtra("taskID", task.getId());
            intent.putExtra("lat", task.getLatitude());
            intent.putExtra("long", task.getLongitude());
            intent.putExtra("destination", task.getAddress());
            intent.putExtra("radiusType", task.getRadius_type());
            intent.putExtra("original", task.getOriginalRadius());
            intent.putExtra("contact",contactInfo);
            startActivity(intent);
        }

        if(task.getTaskType()== Task.SOUND_SETTING_TASK_TYPE){
            List<SoundSettings> sounds = taskSoundDataSource.getTaskSoundsByTaskId(task.getId());
            SoundSettings sound = sounds.get(0);
            Intent intent = new Intent(this, EditSoundTaskActivity.class);
            intent.putExtra("taskID", task.getId());
            intent.putExtra("lat", task.getLatitude());
            intent.putExtra("long", task.getLongitude());
            intent.putExtra("destination", task.getAddress());
            intent.putExtra("radiusType", task.getRadius_type());
            intent.putExtra("original", task.getOriginalRadius());
            intent.putExtra("ring", sound.getRinger());
            intent.putExtra("media", sound.getMedia());
            intent.putExtra("system", sound.getAlarm());
            intent.putExtra("notify", sound.getNotification());
            startActivity(intent);
        }
        if(task.getTaskType() == Task.REMINDER_MESSAGE_TASK_TYPE){
            Intent intent = new Intent(this,EditReminderTaskActivity.class);
            intent.putExtra("taskID", task.getId());
            intent.putExtra("lat", task.getLatitude());
            intent.putExtra("long", task.getLongitude());
            intent.putExtra("destination", task.getAddress());
            intent.putExtra("radiusType", task.getRadius_type());
            intent.putExtra("original", task.getOriginalRadius());
            intent.putExtra("message", task.getReminder());
            startActivity(intent);
        }

        super.onListItemClick(l, v, position, id);
    }

    public void createTasks(View view){
        Intent intent = new Intent(this,MyActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        tasks = taskDataSource.getAllTasks();
        TaskAdapter adapter = new TaskAdapter(this,
                R.layout.task_reminder_item,tasks);
        setListAdapter(adapter);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_location_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_location:
                Intent intent = new Intent(this, MyActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.menu_settings:
                Intent intent1 = new Intent(this, UserSettingsActivity.class);
                startActivity(intent1);
                return true;
            case R.id.list:
                Intent intent2 = new Intent(this, TaskListActivity.class);
                startActivity(intent2);
                return true;
            case R.id.listLocations:
                Intent intent3 = new Intent(this, MyLocationsActivity.class);
                startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
