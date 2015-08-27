/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.frames;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.activities.WelcomeActivity;


public class SignInFrame extends Fragment {

    private WelcomeActivity welcome;

    public void setActivity(WelcomeActivity welcome) {
        this.welcome = welcome;
    }

    @Override
    public void onResume() {
        PreyLogger.i("onResume of SignInFrame");
        super.onResume();
    }

    @Override
    public void onPause() {
        PreyLogger.i("OnPause of SignInFrame");
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signin, container, false);

        Button button = (Button) view.findViewById(R.id.buttonSignin);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PreyConfig.getPreyConfig(getActivity()).setProtectAccount(true);
                welcome.menu();
            }
        });

        TextView linkSignin = (TextView) view.findViewById(R.id.linkSignin);
        linkSignin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                welcome.signUp();
            }
        });
        return view;
    }

}
