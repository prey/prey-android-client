/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import com.prey.R;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AgreementDialogActivity extends PreyActivity {

    protected static final int INSTRUCTIONS_SENT = 0;
    private int wrongPasswordIntents = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agreement);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TextView linkToTOS = (TextView) findViewById(R.id.linkToTosText);
        linkToTOS.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String url = "http://" + getPreyConfig().getPreyDomain() + "/terms";
                Intent internetIntent = new Intent(Intent.ACTION_VIEW);
                internetIntent.setData(Uri.parse(url));
                startActivity(internetIntent);

            }
        });

        Button checkPasswordOkButton = (Button) findViewById(R.id.agree_tos_button);
        checkPasswordOkButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();

            }
        });

        Button checkPasswordCancelButton = (Button) findViewById(R.id.dont_agree_tos_button);
        checkPasswordCancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

    }
}
