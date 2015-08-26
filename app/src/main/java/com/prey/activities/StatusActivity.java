package com.prey.activities;

/**
 * Created by oso on 25-08-15.
 */

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.PreyStatus;
import com.prey.actions.location.PreyLocationManager;
import com.prey.services.PreyRunnerService;
import com.prey.R;
public class StatusActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        fillData();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(StatusActivity.this, PreyConfigurationActivity.class);
        PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
        startActivity(intent);
    }

    private void fillData(){
        TableLayout table = (TableLayout)findViewById(R.id.status_table);
        boolean isRunning = PreyRunnerService.running;
        String running = isRunning ? getString(R.string.running) : getString(R.string.stopped);
        String runningSince = "-";
        String elapsedTime = "-";
        if (isRunning) {
            long diff = System.currentTimeMillis() - PreyRunnerService.startedAt;
            long minutes = diff / (60 * 1000);
            long hours = diff / (60 * 60 * 1000);
            runningSince = getDateStarted();
            elapsedTime = hours + " " + getString(R.string.hours) + ", " + minutes + " " + getString(R.string.minutes);

            diff = PreyRunnerService.interval * 60 * 1000 - (System.currentTimeMillis() - PreyRunnerService.pausedAt);
            long nextRun = diff / (60 * 1000);

            ((TextView) findViewById(R.id.status_execution_interval)).setText(PreyRunnerService.interval + " " + getString(R.string.minutes));
            ((TextView) findViewById(R.id.status_next_execution_in)).setText(nextRun + " " + getString(R.string.minutes));
            ((TextView) findViewById(R.id.status_running_since)).setText(runningSince);
            ((TextView) findViewById(R.id.status_elapsed_time)).setText(elapsedTime);

        } else {
            table.removeView(findViewById(R.id.status_execution_interval_row));
            table.removeView(findViewById(R.id.status_next_execution_in_row));
            table.removeView(findViewById(R.id.status_running_since_row));
            table.removeView(findViewById(R.id.status_elapsed_time_row));
        }

        boolean gps = PreyLocationManager.getInstance(getApplicationContext()).isGpsLocationServiceActive();
        boolean net = PreyLocationManager.getInstance(getApplicationContext()).isNetworkLocationServiceActive();

        String gpsActive = gps ? getString(R.string.enabled) : getString(R.string.disabled);
        String networkActive = net ? getString(R.string.enabled) : getString(R.string.disabled);

        if (gps || net)
            table.removeView(findViewById(R.id.status_no_ls_active_row));


        ((TextView) findViewById(R.id.status_running)).setText(running);
        ((TextView) findViewById(R.id.status_gps_ls_active)).setText(gpsActive);
        ((TextView) findViewById(R.id.status_network_ls_active)).setText(networkActive);

    }

    private String getDateStarted() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date now = new Date(PreyRunnerService.startedAt);
        return sdfDate.format(now);
    }

}

