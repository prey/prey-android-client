package com.prey.activities.frames;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prey.PreyLogger;
import com.prey.R;
import com.prey.activities.WelcomeActivity;

/**
 * Created by oso on 19-08-15.
 */
public class DeviceReadyFrame  extends Fragment {


    private WelcomeActivity welcome;







    public void setActivity(WelcomeActivity welcome) {
        this.welcome = welcome;
    }


    @Override
    public void onResume() {
        PreyLogger.i("onResume of ProtectedFrame");
        super.onResume();
    }

    @Override
    public void onPause() {
        PreyLogger.i("OnPause of ProtectedFrame");
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        View view = inflater.inflate(R.layout.device_ready, container, false);
        return view;
    }
}
