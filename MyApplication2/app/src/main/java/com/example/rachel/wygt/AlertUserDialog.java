package com.example.rachel.wygt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
* Created by Rachel on 10/8/14.
*/
    public class AlertUserDialog extends DialogFragment implements DialogInterface.OnClickListener {
        private String _displayMessage;
        private String _settingsActivityAction;

        public AlertUserDialog(){

        }

        public AlertUserDialog(String displayMessage, String settingsActivityAction){
            _displayMessage = displayMessage != null ? displayMessage : "MESSAGE NOT SET";
            _settingsActivityAction = settingsActivityAction;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(_displayMessage);
            builder.setPositiveButton("OK", this);

            Dialog theDialog = builder.create();
            theDialog.setCanceledOnTouchOutside(false);

            return theDialog;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case Dialog.BUTTON_POSITIVE:
                    //perform desired action response to user clicking "OK"

                    if(_settingsActivityAction != null){
                        startActivity(new Intent(_settingsActivityAction));
                    }
                    break;
            }
        }
    }

