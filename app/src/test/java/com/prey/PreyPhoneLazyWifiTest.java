/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

/**
 * Tests for the lazy WiFi initialization in {@link PreyPhone}.
 *
 * <p>Background: every {@code new PreyPhone(ctx)} used to call
 * {@code WifiManager.getScanResults()} from its constructor, regardless of
 * whether the caller actually wanted WiFi info. Half of the call sites
 * (PreyConfig.resolveImei, PreyWebServices.sendPreyHttpData hardware-only
 * branch, PrivateIp, PreyTelephonyManager) only need hardware/IP/telephony
 * data — paying for a scan-results call there meant the system attributed
 * extra WifiScanTime to the app's UID for no functional reason.
 *
 * <p>The fix defers {@code updateListWifi} and {@code updateWifi} until the
 * matching getter is actually called. These tests pin that contract so a
 * future refactor can't silently re-introduce the eager scan.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class PreyPhoneLazyWifiTest {

    @Test
    public void constructor_doesNotPopulateWifiState() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();

        PreyPhone phone = new PreyPhone(context);

        assertNull(
                "Constructing PreyPhone must NOT touch WiFi state — that call "
                        + "gets attributed to our UID by the BatteryStats service",
                readField(phone, "listWifi")
        );
        assertNull(
                "Constructing PreyPhone must NOT populate the active Wifi info either",
                readField(phone, "wifi")
        );
    }

    @Test
    public void getHardware_doesNotTriggerWifiInit() throws Exception {
        // The most common hot-path caller (PreyWebServices.sendPreyHttpData on
        // every report) only wants hardware. Reading hardware must not pull
        // WiFi state behind it.
        Context context = ApplicationProvider.getApplicationContext();
        PreyPhone phone = new PreyPhone(context);

        phone.getHardware();

        assertNull(
                "getHardware() must not lazily pull in WiFi state",
                readField(phone, "listWifi")
        );
        assertNull(
                "getHardware() must not lazily pull in active Wifi info",
                readField(phone, "wifi")
        );
    }

    @Test
    public void getListWifi_populatesOnFirstCall() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        PreyPhone phone = new PreyPhone(context);

        // Sanity: starts null because the constructor no longer initializes it.
        assertNull(readField(phone, "listWifi"));

        Object result = phone.getListWifi();

        assertNotNull(
                "getListWifi() must lazily initialize on first call so legitimate "
                        + "callers (AccessPointsList, LocationUtil) still observe the data",
                result
        );
        assertNotNull(
                "Field must remain populated after the first lazy-init",
                readField(phone, "listWifi")
        );
    }

    @Test
    public void getWifi_populatesOnFirstCall() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        PreyPhone phone = new PreyPhone(context);

        assertNull(readField(phone, "wifi"));

        Object result = phone.getWifi();

        assertNotNull("getWifi() must lazily initialize", result);
        assertNotNull(readField(phone, "wifi"));
    }

    private static Object readField(PreyPhone phone, String name) throws Exception {
        Field f = PreyPhone.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(phone);
    }
}
