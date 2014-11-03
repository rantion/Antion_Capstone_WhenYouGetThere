package com.example.rachel.wygt;

/**
 * Created by Rachel on 11/1/14.
 */
public class SoundSettings {
    public static final int SOUND_TYPE_RINGER = 1;
    public static final int SOUND_TYPE_MEDIA = 2;
    public static final int SOUND_TYPE_ALARM = 3;
    public static final int SOUND_TYPE_NOTIFICATION = 4;
    public long id;
    public long taskId;

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private int media, ringer, alarm, notification;

    public int getMedia() {
        return media;
    }

    public void setMedia(int media) {
        this.media = media;
    }

    public int getRinger() {
        return ringer;
    }

    public void setRinger(int ringer) {
        this.ringer = ringer;
    }

    public int getAlarm() {
        return alarm;
    }

    public void setAlarm(int alarm) {
        this.alarm = alarm;
    }

    public int getNotification() {
        return notification;
    }

    public void setNotification(int notification) {
        this.notification = notification;
    }
}
