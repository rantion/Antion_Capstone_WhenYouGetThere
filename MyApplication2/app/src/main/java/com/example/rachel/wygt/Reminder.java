package com.example.rachel.wygt;

/**
 * Created by Rachel on 10/27/14.
 */
public class Reminder extends Task{

    private final int REMINDER_TYPE_MESSAGE = 0;
    private final int REMINDER_TYPE_CHECKLIST = 1;
    public int ReminderType;

    public Reminder(){

    }

    public int getReminderType() {
        return ReminderType;
    }

    public void setReminderType(int reminderType) {
        ReminderType = reminderType;
    }

    public int getREMINDER_TYPE_CHECKLIST() {
        return REMINDER_TYPE_CHECKLIST;
    }

    public int getREMINDER_TYPE_MESSAGE() {
        return REMINDER_TYPE_MESSAGE;
    }
}
