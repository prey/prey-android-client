/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.managers;

import android.content.Context;

import android.view.Display;
import android.view.WindowManager;

public class PreyWindowsManager {

    private static PreyWindowsManager _instance = null;

    private int width = 0;
    private int height = 0;

    public static PreyWindowsManager getInstance(Context ctx){
        if(_instance==null){
            _instance=new PreyWindowsManager(ctx);
        }
        return _instance;
    }
    private PreyWindowsManager(Context ctx) {
        WindowManager window = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

}

