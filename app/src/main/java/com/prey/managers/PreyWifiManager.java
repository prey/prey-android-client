/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.prey.PreyLogger;

public class PreyWifiManager {

    private WifiManager wifiMgr=null;
    private static PreyWifiManager _instance = null;
    private Context ctx=null;

    private PreyWifiManager(Context ctx) {
        this.ctx=ctx;
        wifiMgr = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
    }

    public static PreyWifiManager getInstance(Context ctx) {
        if (_instance == null)
            _instance = new PreyWifiManager(ctx);
        return _instance;
    }

    public boolean isWifiEnabled(){
        if(wifiMgr!=null)
            return wifiMgr.isWifiEnabled();
        else
            return false;
    }

    public  boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public WifiInfo getConnectionInfo(){
        try{
            if(wifiMgr!=null)
                return wifiMgr.getConnectionInfo();
        }catch(Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        return null;
    }

    public String getSSID(){
        if (getConnectionInfo()!=null){
            String ssid= getConnectionInfo().getSSID();
            if(ssid!=null){
                ssid=ssid.replace("\"", "");
            }
            return ssid;
        }
        return null;
    }

}