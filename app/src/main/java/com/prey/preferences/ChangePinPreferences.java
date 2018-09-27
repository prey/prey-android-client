/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.activities.PreyConfigurationActivity;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;

import org.json.JSONObject;

public class ChangePinPreferences extends DialogPreference {

    View changePin = null;

    public ChangePinPreferences(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChangePinPreferences(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater i = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        changePin = i.inflate(R.layout.set_pin, null);
        EditText pinEdit=(EditText) changePin.findViewById(R.id.pin_edit);
        String pinNumber=PreyConfig.getPreyConfig(getContext()).getPinNumber();
        if(pinNumber!=null&&!"".equals(pinNumber)){
            try{pinEdit.setHint(pinNumber );}catch (Exception e){PreyLogger.e("Error:"+e.getMessage(),e);}
        }else{
            pinEdit.setHint("");
        }
        return changePin;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if (changePin != null && which == DialogInterface.BUTTON_POSITIVE) {

            final String pinEdit = ((EditText) changePin.findViewById(R.id.pin_edit)).getText().toString();


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new ChangePin().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pinEdit);
            else
                new ChangePin().execute(pinEdit);

        }
    }

    private class ChangePin extends AsyncTask<String, Void, Void> {

        private String error = null;
        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getContext().getText(R.string.preference_pin_updating_dialog).toString());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... array) {
            String pin=array[0];
            PreyLogger.d("pin:" + pin);
            try {
                PreyConfig.getPreyConfig(getContext()).setPinNumber(pin);
                try {
                    if(!"".equals(pin)) {
                        PreyLogger.d("EVENT pin_:" + pin);
                        JSONObject info = new JSONObject();
                        info.put("pin", pin);
                        Event event = new Event(Event.PIN_CHANGED, info.toString());
                        new EventManagerRunner(getContext(), event).run();
                    }
                } catch (Exception e1) {
                }
            } catch (Exception e) {
                PreyConfig.getPreyConfig(getContext()).setPinNumber("");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            progressDialog.dismiss();
            if (error == null) {
                String pinNumber=PreyConfig.getPreyConfig(getContext()).getPinNumber();
                if(pinNumber!=null&&!"".equals(pinNumber)) {
                    Toast.makeText(getContext(), R.string.preference_pin_successfully_changed, Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(getContext(), R.string.preference_pin_removed, Toast.LENGTH_LONG).show();
                    PreyConfig.getPreyConfig(getContext()).setSmsCommand(false);
                }
                Intent intent = new Intent(getContext(), PreyConfigurationActivity.class);
                PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
                getContext().startActivity(intent);
            } else {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                showDialog(new Bundle());
            }
        }

    }

}

