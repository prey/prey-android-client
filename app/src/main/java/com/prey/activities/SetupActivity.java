package com.prey.activities;

/**
 * Created by oso on 25-08-15.
 */

import android.content.Context;
import android.content.Intent;

import com.prey.PreyUtils;

public class SetupActivity extends PreyActivity {

    protected String getDeviceType(Context ctx) {
        return PreyUtils.getDeviceType(ctx);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SetupActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

}

