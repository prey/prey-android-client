/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.frames;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.activities.WelcomeActivity;

public class MenuFrame extends Fragment {

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
        View view = inflater.inflate(R.layout.menu, container, false);

        ImageView imgViewSetup = (ImageView) view.findViewById(R.id.imgViewSetup);
        ImageView imgViewPrivileges = (ImageView) view.findViewById(R.id.imgViewPrivileges);
        Button buttonActivate = (Button) view.findViewById(R.id.buttonActivate);
        Button buttonMore = (Button) view.findViewById(R.id.buttonMore);

        boolean account = PreyConfig.getPreyConfig(getActivity()).getProtectAccount();
        boolean privileges = PreyConfig.getPreyConfig(getActivity()).getProtectPrivileges();
        //boolean tour = PreyConfig.getPreyConfig(getActivity()).getProtectTour();

        if (account) {
            imgViewSetup.setImageResource(R.drawable.ok);
        } else {
            imgViewSetup.setImageResource(R.drawable.nok);
        }
        if (privileges) {
            imgViewPrivileges.setImageResource(R.drawable.ok);
        } else {
            imgViewPrivileges.setImageResource(R.drawable.nok);
        }

        if (account && privileges) {
            buttonActivate.setVisibility(View.VISIBLE);
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                buttonActivate.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2A5E93")));
            } else {
                buttonActivate.setBackground(new ColorDrawable(Color.parseColor("#2A5E93")));
            }
            buttonActivate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    welcome.ready();
                    PreyConfig.getPreyConfig(getActivity()).setProtectReady(true);
                }
            });
        } else {
            buttonActivate.setVisibility(View.VISIBLE);
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                buttonActivate.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#C1BDBB")));
            } else {
                buttonActivate.setBackground(new ColorDrawable(Color.parseColor("#C1BDBB")));
            }
        }

        buttonMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                welcome.tour();
            }
        });

        LinearLayout linearLayoutSetup = (LinearLayout) view.findViewById(R.id.linearLayoutSetup);
        linearLayoutSetup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                welcome.signIn();

            }
        });

        LinearLayout linearLayoutPrivileges = (LinearLayout) view.findViewById(R.id.linearLayoutPrivileges);
        linearLayoutPrivileges.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                welcome.privileges();
            }
        });

        return view;
    }

}
