package com.prey.activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

import com.prey.PreyConfig;
import com.prey.PreyUtils;

/**
 * Created by oso on 24-08-15.
 */
public class PreyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected PreyConfig getPreyConfig(){
        return PreyConfig.getPreyConfig(PreyActivity.this);
    }

    protected String getDeviceType() {
        return PreyUtils.getDeviceType(this);
    }

}
