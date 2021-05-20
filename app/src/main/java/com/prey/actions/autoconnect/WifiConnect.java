/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.autoconnect;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.prey.PreyLogger;

import java.util.ArrayList;
import java.util.List;

public class WifiConnect {

    private Context ctx;
    private WifiManager mWifiManager;

    public WifiConnect(Context ctx) {
        this.ctx = ctx;
        mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
    }

    public void scan(){
        if (mWifiManager.isWifiEnabled() == false) {
            mWifiManager.setWifiEnabled(true);
            try { Thread.sleep(4000);} catch (Exception e) { }
        }
        List<ScanResult> results = mWifiManager.getScanResults();
        PreyLogger.d("results size:" + results.size());
        for (int i = 0; results != null && i < results.size(); i++) {
            ScanResult scan = results.get(i);
            if ("[ESS]".equals(scan.capabilities)&&scan.SSID.indexOf("Prey")<0) {
                PreyLogger.d("ssid:" + scan.SSID + " " + scan.capabilities + " ");
                //WifiConfiguration conf = new WifiConfiguration();
                String ssid =   scan.SSID  ;
                //wifiManager.addNetwork(conf);
                connect(ctx,ssid);
                // break;
            }
        }
    }

    public void onScanResultsAvailable() {
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        ArrayList<BriefWiFiInfo> wiFiList = new ArrayList<>();
        for (ScanResult scanResult : scanResults) {
            BriefWiFiInfo briefWiFiInfo = new BriefWiFiInfo();
            briefWiFiInfo.setSsid(scanResult.SSID);
            int rssiPercentage = WifiManager.calculateSignalLevel(scanResult.level, 100);
            briefWiFiInfo.setRssi(rssiPercentage);
            wiFiList.add(briefWiFiInfo);
        }
    }

    public void connect(Context ctx,String networkSSID) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"".concat(networkSSID).concat("\"");
        config.status = WifiConfiguration.Status.DISABLED;
        config.priority = 40;
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedAuthAlgorithms.clear();
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        int networkId = wifiManager.addNetwork(config);
        if (networkId != -1) {
            boolean isDisconnected = wifiManager.disconnect();
            PreyLogger.d("isDisconnected : " + isDisconnected);
            boolean isEnabled = wifiManager.enableNetwork(networkId, true);
            PreyLogger.d("isEnabled : " + isEnabled);
            boolean isReconnected = wifiManager.reconnect();
        }

    }

}