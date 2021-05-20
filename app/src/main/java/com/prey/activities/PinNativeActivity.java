/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;

import java.util.Calendar;
import java.util.Date;

public class PinNativeActivity extends Activity {

    Button button_Super_Lock_Unlock = null;
    Button button_close = null;
    TextView textViewPin = null;
    EditText editTextPin = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.super_lock);
        PreyLogger.d("PinNativeActivity: onCreate");
        editTextPin = (EditText) findViewById(R.id.editTextPin);
        textViewPin = (TextView) findViewById(R.id.textViewPin);
        button_Super_Lock_Unlock = (Button) findViewById(R.id.button_Super_Lock_Unlock);
        button_close = (Button) findViewById(R.id.button_close);
        Typeface regularBold = Typeface.createFromAsset(getAssets(), "fonts/Regular/regular-bold.otf");
        Typeface regularBook = Typeface.createFromAsset(getAssets(), "fonts/Regular/regular-book.otf");
        editTextPin.setTypeface(regularBold);
        textViewPin.setTypeface(regularBook);
        button_Super_Lock_Unlock.setTypeface(regularBook);
        editTextPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editTextPin.setBackgroundColor(Color.WHITE);
            }
            @Override
            public void afterTextChanged(Editable s) {
                editTextPin.setBackgroundColor(Color.WHITE);
            }
        });
        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        button_Super_Lock_Unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pin = editTextPin.getText().toString();
                if (pin != null) {
                    String pinNumber = PreyConfig.getPreyConfig(getApplicationContext()).getPinNumber();
                    PreyLogger.d("pinNumber:" + pinNumber + " pin:" + pin);
                    if (pinNumber.equals(pin)) {
                        PreyConfig.getPreyConfig(getApplicationContext()).setPinActivated("");
                        PreyConfig.getPreyConfig(getApplicationContext()).setCounterOff(0);
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(new Date().getTime());
                        cal.add(Calendar.MINUTE, 1);
                        PreyConfig.getPreyConfig(getApplicationContext()).setTimeSecureLock(cal.getTimeInMillis());
                        finish();
                    } else {
                        PreyLogger.d("error");
                        editTextPin.setBackgroundColor(Color.RED);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String pinActivated = PreyConfig.getPreyConfig(getApplicationContext()).getPinActivated();
        PreyLogger.d("PinNativeActivity unlock:" + pinActivated);
        if (pinActivated == null || "".equals(pinActivated)) {
            Intent intent = new Intent(getApplicationContext(), CloseActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
        }
    }

}