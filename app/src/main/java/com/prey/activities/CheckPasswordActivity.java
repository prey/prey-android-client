/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.R;

public class CheckPasswordActivity extends PasswordActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        bindPasswordControls();
        TextView device_ready_h2_text=(TextView)findViewById(R.id.device_ready_h2_text);
        final  TextView textForgotPassword = (TextView) findViewById(R.id.link_forgot_password);
        final  TextView textLink_privacy = (TextView) findViewById(R.id.link_privacy);
        final  TextView textLink_uninstall_prey = (TextView) findViewById(R.id.link_uninstall_prey);

        Button password_btn_login=(Button)findViewById(R.id.password_btn_login);
        EditText password_pass_txt=(EditText)findViewById(R.id.password_pass_txt);
        Typeface titilliumWebRegular = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
        Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");
        Typeface magdacleanmonoRegular = Typeface.createFromAsset(getAssets(), "fonts/MagdaClean/magdacleanmono-regular.ttf");
        device_ready_h2_text.setTypeface(titilliumWebRegular);
        textForgotPassword.setTypeface(titilliumWebBold);
        textLink_privacy.setTypeface(titilliumWebBold);
        textLink_uninstall_prey.setTypeface(titilliumWebBold);
        password_btn_login.setTypeface(titilliumWebBold);
        password_pass_txt.setTypeface(magdacleanmonoRegular);
        try {
            textForgotPassword.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    try {
                        String url = PreyConfig.getPreyConfig(getApplicationContext()).getPreyPanelUrl();
                        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                        startActivity(browserIntent);
                    } catch (Exception e) {
                    }
                }
            });
            textLink_privacy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    startActivity(new Intent(getApplicationContext(), PrivacyActivity.class));
                    finish();
                }
            });
        } catch (Exception e) {
        }

        textLink_uninstall_prey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                String url=getApplicationContext().getString(R.string.uninstall_prey_link);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }

    public void onBackPressed() {
        Intent intent = null;
        intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

}