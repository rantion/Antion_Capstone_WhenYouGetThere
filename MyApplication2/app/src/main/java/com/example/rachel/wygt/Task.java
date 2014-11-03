package com.example.rachel.wygt;

/**
 * Created by Rachel on 10/22/14.
 */
public class Task {
    private double latitude, longitude;
    private long radius;
    private long id;
    private long time = System.currentTimeMillis();
    private String reminder;
    private int TaskType;
    public static final int TEXT_MESSAGE_TASK_TYPE = 1;
    public static final int REMINDER_MESSAGE_TASK_TYPE = 2;
    public static final int CALL_REMINDER_TASK_TYPE = 3;
    public static final int SOUND_SETTING_TASK_TYPE= 4;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getRadius() {
        return radius;
    }

    public void setRadius(long radius) {
        this.radius = radius;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public int getTaskType() {
        return TaskType;
    }

    public void setTaskType(int taskType) {
        TaskType = taskType;
    }

    public static int getTextMessageTaskType() {
        return TEXT_MESSAGE_TASK_TYPE;
    }

    public static int getReminderMessageTaskType() {
        return REMINDER_MESSAGE_TASK_TYPE;
    }
}
