/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class PasswordNativeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.lock_android7);
        PreyLogger.d("PasswordActivity2: onCreate");
        final EditText editText = (EditText) findViewById(R.id.EditText_Lock_Password);
        final Button unlockButton = findViewById(R.id.Button_Lock_Unlock);
        final ImageView imageLock = findViewById(R.id.ImageView_Lock_AccessDenied);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            PreyLogger.d("Software Keyboard was shown");
        } else {
            PreyLogger.d("Software Keyboard was not shown");
        }
        final TextView text = findViewById(R.id.TextView_Lock_AccessDenied);
        final View contentView = findViewById(android.R.id.content);
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int mPreviousHeight;

            @Override
            public void onGlobalLayout() {
                int newHeight = contentView.getHeight();
                if (mPreviousHeight != 0) {
                    if (mPreviousHeight > newHeight) {
                        PreyLogger.d("Software Keyboard was shown");
                        imageLock.setVisibility(View.GONE);
                    } else if (mPreviousHeight < newHeight) {
                        PreyLogger.d("Software Keyboard was not shown");
                        imageLock.setVisibility(View.VISIBLE);
                    }
                }
                mPreviousHeight = newHeight;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                text.setText(R.string.lock_access_denied);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String unlock = PreyConfig.getPreyConfig(getApplicationContext()).getUnlockPass();
                    String key = editText.getText().toString().trim();
                    PreyLogger.d("PasswordActivity2 unlock key:" + key + " unlock:" + unlock);
                    if (unlock != null && unlock.equals(key)) {
                        PreyConfig.getPreyConfig(getApplicationContext()).setUnlockPass("");
                        new Thread() {
                            public void run() {
                                String reason = "{\"origin\":\"user\"}";
                                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(getApplicationContext(), UtilJson.makeMapParam("start", "lock", "stopped", reason));
                            }
                        }.start();
                        onResume();
                    } else {
                        if (unlock == null) {
                            onResume();
                        } else {
                            text.setText(R.string.password_wrong);
                        }
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String unlock = PreyConfig.getPreyConfig(getApplicationContext()).getUnlockPass();
        PreyLogger.d("PasswordActivity2 unlock:" + unlock);
        if (unlock == null || "".equals(unlock)) {
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