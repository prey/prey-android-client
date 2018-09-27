/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events;

public class Event {

    public static final String SIM_CHANGED = "sim_changed";
    public static final String WIFI_CHANGED = "ssid_changed";
    public static final String TURNED_ON = "device_turned_on";
    public static final String TURNED_OFF = "device_turned_off";
    public static final String BATTERY_LOW = "low_battery";
    public static final String APPLICATION_OPENED = "prey_opened";
    public static final String BATTERY_OK = "ok_battery";
    public static final String BATTERY_CHANGE = "change_battery";
    public static final String POWER_CONNECTED = "power_connected";
    public static final String POWER_DISCONNECTED = "power_disconnected";
    public static final String PIN_CHANGED = "pin_changed";

    private String name;
    private String info;

    public Event() {

    }

    public Event(String name) {
        this.name = name;
        this.info = "";
    }


    public Event(String name, String info) {
        this.name = name;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

}

