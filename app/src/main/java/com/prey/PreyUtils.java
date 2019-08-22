/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import java.util.Locale;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.widget.Toast;

public class PreyUtils {
    public static String getDeviceType(Activity act) {
        return getDeviceType(act.getApplicationContext());
    }

    public static String getDeviceType(Context ctx) {
        if (isChromebook(ctx)) {
            return "Laptop";
        } else {
            if (isTablet(ctx)) {
                return "Tablet";
            } else {
                return "Phone";
            }
        }
    }

    public static boolean isChromebook(Context ctx) {
        return PreyConfig.getPreyConfig(ctx).isChromebook();
    }

    public static boolean isTablet(Context ctx) {
        try {
            DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
            float screenWidth = dm.widthPixels / dm.xdpi;
            float screenHeight = dm.heightPixels / dm.ydpi;
            double size = Math.sqrt(Math.pow(screenWidth, 2) + Math.pow(screenHeight, 2));
            return size >= 6;
        } catch (Throwable t) {
            return false;
        }
    }

    public static String randomAlphaNumeric(int length) {
        StringBuffer buffer = new StringBuffer();
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        int charactersLength = characters.length();
        for (int i = 0; i < length; i++) {
            double index = Math.random() * charactersLength;
            buffer.append(characters.charAt((int) index));
        }
        return buffer.toString();
    }

    public static String getBuildVersionRelease() {
        String version = "";
        try {
            String release = Build.VERSION.RELEASE;
            StringTokenizer st = new StringTokenizer(release, ".");
            boolean first = true;
            while (st.hasMoreElements()) {
                String number = st.nextToken();
                //if (number != null)
                    //number = number.substring(0, 1);
                version = (first) ? number : version + "." + number;
                first = false;
            }
        } catch (Exception e) {
        }
        return version;
    }

    public static String getLanguage(){
        return "es".equals(Locale.getDefault().getLanguage()) ? "es" : "en";
    }

    public static void toast(final Context ctx,final String out){
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(out!=null&& !"".equals(out)) {
                    Toast.makeText(ctx, out, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
