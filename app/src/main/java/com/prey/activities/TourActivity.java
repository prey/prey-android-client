/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.prey.PreyConfig;
import com.prey.R;

public class TourActivity extends Activity {

    private int id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        id = b.getInt("id");

        switch (id){
            case 2:
                setContentView(R.layout.tour2);
                break;
            case 3:
                setContentView(R.layout.tour3);
                break;
            case 4:
                setContentView(R.layout.tour4);
                break;
            case 5:
                setContentView(R.layout.tour5);
                break;
            default:
                setContentView(R.layout.tour1);
                break;
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        Button buttonTour=(Button)findViewById(R.id.buttontour01);
        buttonTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=null;
                if (id==6){
                    PreyConfig.getPreyConfig(getApplication()).setProtectTour(true);
                    intent=new Intent(getApplicationContext(),WelcomeActivity.class);
                }else{
                    intent=new Intent(getApplicationContext(),TourActivity.class);
                }
                Bundle b = new Bundle();
                b.putInt("id", id + 1);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });


        try{
            ImageView close=(ImageView)findViewById(R.id.close_tour1);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PreyConfig.getPreyConfig(getApplication()).setProtectTour(true);
                    Intent intent=new Intent(getApplicationContext(),WelcomeActivity.class);
                    startActivity(intent);
                    finish();
                }


            });
        }catch(Exception e){

        }






    }

    public void onBackPressed() {
        Intent intent=null;
        if (id>1){
            intent=new Intent(getApplicationContext(),TourActivity.class);
            Bundle b = new Bundle();
            b.putInt("id", id-1);
            intent.putExtras(b);
        }else{

            intent=new Intent(getApplicationContext(),WelcomeActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
