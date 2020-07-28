/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.prey.R;

public class PermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_permission);
        final TextView permiso_link = (TextView) findViewById(R.id.permiso_link);
        final ImageView arrow1 = (ImageView) findViewById(R.id.imageView2);
        final ImageView arrow2 = (ImageView) findViewById(R.id.imageView3);
        final ImageView arrow3 = (ImageView) findViewById(R.id.imageView4);
        permiso_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PermissionInformationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        arrow1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView2_2 = (TextView) findViewById(R.id.textView2_2);
                if (textView2_2.getVisibility() == View.GONE) {
                    textView2_2.setVisibility(View.VISIBLE);
                    arrow1.setImageResource(R.drawable.up);
                } else {
                    textView2_2.setVisibility(View.GONE);
                    arrow1.setImageResource(R.drawable.down);
                }
            }
        });
        arrow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView3_2 = (TextView) findViewById(R.id.textView3_2);
                if (textView3_2.getVisibility() == View.GONE) {
                    textView3_2.setVisibility(View.VISIBLE);
                    arrow2.setImageResource(R.drawable.up);
                } else {
                    textView3_2.setVisibility(View.GONE);
                    arrow2.setImageResource(R.drawable.down);
                }
            }
        });
        arrow3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView4_2 = (TextView) findViewById(R.id.textView4_2);
                if (textView4_2.getVisibility() == View.GONE) {
                    textView4_2.setVisibility(View.VISIBLE);
                    arrow3.setImageResource(R.drawable.up);
                } else {
                    textView4_2.setVisibility(View.GONE);
                    arrow3.setImageResource(R.drawable.down);
                }
            }
        });
    }

}