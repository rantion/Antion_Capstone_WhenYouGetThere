package com.example.rachel.wygt;

/**
 * Created by Rachel on 10/27/14.
 */
public class ReminderMessage extends Reminder {
    private CharSequence reminderContent;

    public ReminderMessage() {
        this.setReminderType(getREMINDER_TYPE_MESSAGE());
    }

    public CharSequence getReminderContent() {
        return reminderContent;
    }

    public void setReminderContent(CharSequence reminderContent) {
        this.reminderContent = reminderContent;
    }
}
