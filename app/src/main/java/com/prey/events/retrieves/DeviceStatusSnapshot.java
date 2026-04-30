/*******************************************************************************
 * Created by Patricio Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.actions.HttpDataService;
import com.prey.actions.battery.Battery;
import com.prey.actions.battery.BatteryInformation;
import com.prey.json.actions.Uptime;
import com.prey.json.actions.Wifi;
import com.prey.managers.PreyConnectivityManager;

import org.json.JSONObject;

import java.util.Map;

public class DeviceStatusSnapshot {

    private DeviceStatusSnapshot() {}

    public static JSONObject build(Context ctx) {
        JSONObject status = new JSONObject();
        putWifi(ctx, status);
        putMobile(ctx, status);
        putOnline(status);
        putBattery(ctx, status);
        putUptime(ctx, status);
        return status;
    }

    private static void putWifi(Context ctx, JSONObject status) {
        try {
            HttpDataService wifiData = new Wifi().run(ctx, null, null);
            Map<String, String> wifiMap = wifiData.getDataList();
            JSONObject ap = new JSONObject();
            ap.put("ssid", wifiMap.get(Wifi.SSID));
            ap.put("signal_strength", wifiMap.get("signal_strength"));
            ap.put("channel", wifiMap.get("channel"));
            ap.put("security", wifiMap.get("security"));
            status.put("active_access_point", ap);
        } catch (Exception e) {
            PreyLogger.e("DeviceStatusSnapshot wifi error: " + e.getMessage(), e);
        }
    }

    private static void putMobile(Context ctx, JSONObject status) {
        try {
            String mobileInternet = "";
            if (PreyConnectivityManager.getInstance(ctx).isMobileConnected()) {
                try {
                    mobileInternet = PreyPhone.getNetworkClass(ctx);
                } catch (Exception e) {
                    PreyLogger.e("DeviceStatusSnapshot networkClass error: " + e.getMessage(), e);
                }
            }
            status.put("mobile_internet", mobileInternet == null ? "" : mobileInternet);
        } catch (Exception e) {
            PreyLogger.e("DeviceStatusSnapshot mobile error: " + e.getMessage(), e);
        }
    }

    private static void putOnline(JSONObject status) {
        try {
            status.put("online", true);
        } catch (Exception e) {
            PreyLogger.e("DeviceStatusSnapshot online error: " + e.getMessage(), e);
        }
    }

    private static void putBattery(Context ctx, JSONObject status) {
        try {
            Intent batteryIntent = ctx.getApplicationContext().registerReceiver(
                    null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (batteryIntent == null) {
                return;
            }
            Battery battery = new BatteryInformation().makeBattery(batteryIntent);
            if (battery == null) {
                return;
            }
            JSONObject batteryJson = new JSONObject();
            batteryJson.put("state", battery.isCharging() ? "charging" : "discharging");
            batteryJson.put("percentage_remaining", Integer.toString(battery.getLevel()));
            status.put("battery_status", batteryJson);
        } catch (Exception e) {
            PreyLogger.e("DeviceStatusSnapshot battery error: " + e.getMessage(), e);
        }
    }

    private static void putUptime(Context ctx, JSONObject status) {
        try {
            HttpDataService uptimeData = new Uptime().run(ctx, null, null);
            String uptime = uptimeData.getSingleData();
            status.put("uptime", uptime);
        } catch (Exception e) {
            PreyLogger.e("DeviceStatusSnapshot uptime error: " + e.getMessage(), e);
        }
    }
}
