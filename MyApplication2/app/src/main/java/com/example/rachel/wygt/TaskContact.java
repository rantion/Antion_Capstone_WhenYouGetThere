package com.example.rachel.wygt;

/**
 * Created by Rachel on 10/28/14.
 */
public class TaskContact {

    private long taskID;
    private String phoneNumber;
    private String name;
    private long id;

    public TaskContact(){

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskID() {
        return taskID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTaskID(long taskID) {
        this.taskID = taskID;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
