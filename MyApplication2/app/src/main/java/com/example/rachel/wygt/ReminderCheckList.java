package com.example.rachel.wygt;

import java.util.List;

/**
 * Created by Rachel on 10/27/14.
 */
public class ReminderCheckList extends Reminder {

    private List<String>checklist;

    public ReminderCheckList(){
    }

    public List<String> getChecklist() {
        return checklist;
    }

    public void setChecklist(List<String> checklist) {
        this.checklist = checklist;
    }

    public void addToCheckList(String newItem){
        checklist.add(newItem);
    }
}
