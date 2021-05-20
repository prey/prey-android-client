/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

public class GoToChangePasswordDialogPreference extends DialogPreference {

    public GoToChangePasswordDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GoToChangePasswordDialogPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if (which == DialogInterface.BUTTON_POSITIVE) {
            String url = PreyConfig.getPreyConfig(getContext()).getPreyUrl() + "profile";
            PreyLogger.d("url change password:" + url);
            Intent internetIntent = new Intent(Intent.ACTION_VIEW);
            internetIntent.setData(Uri.parse(url));
            getContext().startActivity(internetIntent);
        }
    }

}