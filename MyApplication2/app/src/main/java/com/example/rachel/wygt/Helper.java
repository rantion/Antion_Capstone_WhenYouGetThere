package com.example.rachel.wygt;

import android.content.Context;

/**
 * Created by Rachel on 10/23/14.
 */
public class Helper {
    private Context context;

    public Helper(Context context){
       this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
