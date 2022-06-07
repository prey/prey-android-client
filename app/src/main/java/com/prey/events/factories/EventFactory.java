/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.factories;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.R;
import com.prey.actions.aware.AwareController;
import com.prey.actions.geofences.GeofenceController;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocation;
import com.prey.actions.triggers.BatteryTriggerReceiver;
import com.prey.actions.triggers.SimTriggerReceiver;
import com.prey.beta.actions.PreyBetaController;
import com.prey.events.Event;
import com.prey.managers.PreyConnectivityManager;
import com.prey.net.UtilConnection;
import com.prey.services.PreyCloseNotificationService;
import com.prey.services.PreyPermissionService;

public class EventFactory {

    public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public static final String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String WIFI_STATE_CHANGED = "android.net.wifi.WIFI_STATE_CHANGED";
    public static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
    public static final String AIRPLANE_MODE = "android.intent.action.AIRPLANE_MODE";
    public static final String BATTERY_LOW = "android.intent.action.BATTERY_LOW";
    public static final String SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    public static final String USER_PRESENT = "android.intent.action.USER_PRESENT";
    public static final String ACTION_POWER_CONNECTED = "android.intent.action.ACTION_POWER_CONNECTED";
    public static final String ACTION_POWER_DISCONNECTED = "android.intent.action.ACTION_POWER_DISCONNECTED";
    public static final String LOCATION_MODE_CHANGED = "android.location.MODE_CHANGED";
    public static final String LOCATION_PROVIDERS_CHANGED = "android.location.PROVIDERS_CHANGED";
    public static final int NOTIFICATION_ID = 888;

    public static Event getEvent(final Context ctx, Intent intent) {
        String message = "getEvent[" + intent.getAction() + "]";
        PreyLogger.d(message);
        if (BOOT_COMPLETED.equals(intent.getAction())) {
            notification(ctx);
            return new Event(Event.TURNED_ON);
        }
        if (SIM_STATE_CHANGED.equals(intent.getAction())) {
            String state = intent.getExtras().getString(SimTriggerReceiver.EXTRA_SIM_STATE);
            if ("ABSENT".equals(state)) {
                JSONObject info = new JSONObject();
                try {
                    String simSerial = PreyConfig.getPreyConfig(ctx).getSimSerialNumber();
                    if (simSerial != null && !"".equals(simSerial)) {
                        info.put("sim_serial_number", simSerial);
                    }
                } catch (Exception e) {
                    PreyLogger.e("Error:" + e.getMessage(), e);
                }
                new SimTriggerReceiver().onReceive(ctx, intent);
                if (UtilConnection.isInternetAvailable(ctx)) {
                    return new Event(Event.SIM_CHANGED, info.toString());
                } else {
                    return null;
                }
            }
        }
        if (LOCATION_PROVIDERS_CHANGED.equals(intent.getAction()) || LOCATION_MODE_CHANGED.equals(intent.getAction())
        ) {
            new Thread() {
                public void run() {
                    sendLocationAware(ctx);
                }
            }.start();
        }
        if (ACTION_SHUTDOWN.equals(intent.getAction())) {
            return new Event(Event.TURNED_OFF);
        }
        if (BATTERY_LOW.equals(intent.getAction())) {
            new BatteryTriggerReceiver().onReceive(ctx, intent);
            return new Event(Event.BATTERY_LOW);
        }
        if (ACTION_POWER_CONNECTED.equals(intent.getAction())) {
            new BatteryTriggerReceiver().onReceive(ctx, intent);
            return new Event(Event.POWER_CONNECTED);
        }
        if (ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
            new BatteryTriggerReceiver().onReceive(ctx, intent);
            return new Event(Event.POWER_DISCONNECTED);
        }
        if (CONNECTIVITY_CHANGE.equals(intent.getAction())) {
            return null;
        }
        if (WIFI_STATE_CHANGED.equals(intent.getAction())) {
            JSONObject info = new JSONObject();
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            PreyLogger.d("getEvent ___wifiState:" + wifiState);
            try {
                if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    PreyLogger.d("getEvent wifiState connected");
                    info.put("connected", "wifi");
                    PreyBetaController.startPrey(ctx);
                }
                if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    PreyLogger.d("getEvent mobile connected");
                    info.put("connected", "mobile");
                }
            } catch (Exception e) {
                PreyLogger.e("Error getEvent:" + e.getMessage(), e);
            }
            return new Event(Event.WIFI_CHANGED, info.toString());
        }
        if (AIRPLANE_MODE.equals(intent.getAction())) {
            if (!isAirplaneModeOn(ctx)) {
                notification(ctx);
                boolean connected = false;
                if (!PreyConnectivityManager.getInstance(ctx).isWifiConnected()) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        if ("connected".equals(extras.getString(ConnectivityManager.EXTRA_REASON))) {
                            connected = true;
                        }
                    }
                }
                if (!PreyConnectivityManager.getInstance(ctx).isMobileConnected()) {
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                    if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        connected = true;
                    }
                }
                if (connected) {
                    PreyBetaController.startPrey(ctx);
                }
            }
        }
        if (USER_PRESENT.equals(intent.getAction())) {
            PreyLogger.d("EventFactory USER_PRESENT");
            int minuteScheduled = PreyConfig.getPreyConfig(ctx).getMinuteScheduled();
            if (minuteScheduled > 0) {
                PreyBetaController.startPrey(ctx, null);
            }
            return null;
        }
        return null;
    }

    public static void sendLocationAware(final Context ctx) {
        try {
            boolean isTimeLocationAware = PreyConfig.getPreyConfig(ctx).isTimeLocationAware();
            PreyLogger.d("sendLocation isTimeLocationAware:" + isTimeLocationAware);
            if (!isTimeLocationAware) {
                PreyLocation locationNow = LocationUtil.getLocation(ctx, null, false);
                AwareController.sendAware(ctx, locationNow);
                GeofenceController.verifyGeozone(ctx, locationNow);
                PreyConfig.getPreyConfig(ctx).setTimeLocationAware();
            }
        } catch (Exception e) {
            PreyLogger.e("Error sendLocation:" + e.getMessage(), e);
        }
    }

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver
                (), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm:ss", Locale.getDefault());

    public static boolean isValidLowBattery(Context ctx) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MINUTE, -1);
            long leastThreeHours = cal.getTimeInMillis();
            long lowBatteryDate = PreyConfig.getPreyConfig(ctx).getLowBatteryDate();
            PreyLogger.d("lowBatteryDate :" + lowBatteryDate + " " + sdf.format(new Date(lowBatteryDate)));
            PreyLogger.d("leastMinutes   :" + leastThreeHours + " " + sdf.format(new Date(leastThreeHours)));
            if (lowBatteryDate == 0 || leastThreeHours > lowBatteryDate) {
                long now = new Date().getTime();
                PreyConfig.getPreyConfig(ctx).setLowBatteryDate(now);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Method that returns if it has all the permissions
     *
     * @param ctx context
     * @return if you have all permissions
     */
    public static boolean verifyNotification(Context ctx) {
        boolean canAccessCamera = PreyPermission.canAccessCamera(ctx);
        boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(ctx);
        boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(ctx);
        boolean canAccessStorage = PreyPermission.canAccessStorage(ctx);
        return canAccessCamera && (canAccessCoarseLocation || canAccessFineLocation) && canAccessStorage;
    }

    /**
     * Method that opens the notification missing permissions
     *
     * @param ctx context
     */
    public static void notification(Context ctx) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PreyConfig.getPreyConfig(ctx).isThisDeviceAlreadyRegisteredWithPrey(false)) {
                String channelId = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    channelId = "channelPrey2";
                    CharSequence channelName = "Prey2";
                    int channelImportance = NotificationManager.IMPORTANCE_HIGH;
                    boolean channelEnableVibrate = false;
                    int channelLockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE;
                    NotificationChannel notificationChannel =
                            new NotificationChannel(channelId, channelName, channelImportance);
                    notificationChannel.enableVibration(channelEnableVibrate);
                    notificationChannel.setLockscreenVisibility(channelLockscreenVisibility);
                    NotificationManager notificationManager =
                            (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.createNotificationChannel(notificationChannel);
                }
                Intent permissionIntent = new Intent(ctx, PreyPermissionService.class);
                permissionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent permissionPendingIntent = PendingIntent.getService(ctx, 0, permissionIntent, PendingIntent.FLAG_IMMUTABLE);
                NotificationCompat.Action permissionAction =
                        new NotificationCompat.Action.Builder(
                                R.drawable.icon,
                                ctx.getResources().getString(R.string.warning_re_approve),
                                permissionPendingIntent)
                                .build();
                Intent closeIntent = new Intent(ctx, PreyCloseNotificationService.class);
                closeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent closePendingIntent = PendingIntent.getService(ctx, 0, closeIntent, PendingIntent.FLAG_IMMUTABLE);
                NotificationCompat.Action closeAction =
                        new NotificationCompat.Action.Builder(
                                R.drawable.icon,
                                ctx.getResources().getString(R.string.warning_close),
                                closePendingIntent)
                                .build();
                NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                        .bigText(ctx.getResources().getString(R.string.warning_notification_body))
                        .setBigContentTitle(ctx.getResources().getString(R.string.warning_notification_title))
                        .setSummaryText(ctx.getResources().getString(R.string.warning_notification_body));
                NotificationCompat.Builder notificationCompatBuilder =
                        new NotificationCompat.Builder(
                                ctx, channelId);
                Notification notification = notificationCompatBuilder
                        .setStyle(bigTextStyle)
                        .setContentTitle(ctx.getResources().getString(R.string.warning_notification_title))
                        .setContentText(ctx.getResources().getString(R.string.warning_notification_body))
                        .setSmallIcon(R.drawable.icon2)
                        .setLargeIcon(BitmapFactory.decodeResource(
                                ctx.getResources(),
                                R.drawable.icon2))
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
                        .setCategory(Notification.CATEGORY_REMINDER)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .addAction(permissionAction)
                        .addAction(closeAction)
                        .build();
                NotificationManagerCompat mNotificationManagerCompat = NotificationManagerCompat.from(ctx);
                mNotificationManagerCompat.notify(NOTIFICATION_ID, notification);

            }
        }
    }

}