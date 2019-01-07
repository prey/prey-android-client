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
        // mWifiConnectorListener.onWiFiScanResults(wiFiList);
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


/*

    public NetworkInfo.DetailedState connectToWiFi(int securityType, String ssid, String key) {
        Log.d(TAG, "connectToWiFi() called with: securityType = [" + securityType + "], ssid = [" + ssid + "], key = [" + key + "]");
        // Check if already connected to that wifi
        String currentSsid = getActiveConnection().getSSID();
        Log.d(TAG, "Current Ssid " + currentSsid);
        NetworkInfo.DetailedState currentState = WifiInfo.getDetailedStateOf(getActiveConnection().getSupplicantState()); //todo check this
        if (currentState == NetworkInfo.DetailedState.CONNECTED && currentSsid.equals(quoted(ssid))) {
            Log.d(TAG, "Already connected");
            mWifiConnectorListener.onWiFiStateUpdate(getActiveConnection(), NetworkInfo.DetailedState.CONNECTED);
            return NetworkInfo.DetailedState.CONNECTED;
        }

        int highestPriorityNumber = 0;
        WifiConfiguration selectedConfig = null;
        // Check if not connected but has connected to that wifi in the past
        for (WifiConfiguration config : mWifiManager.getConfiguredNetworks()) {
            if (config.priority > highestPriorityNumber) highestPriorityNumber = config.priority;
            if (config.SSID.equals(quoted(ssid)) && config.allowedKeyManagement.get(securityType)) {
                Log.d(TAG, "Saved preshared key is " + config.preSharedKey);
                if (securityType == WifiConfiguration.KeyMgmt.WPA_PSK
                        && config.preSharedKey != null && config.preSharedKey.equals(key))
                    selectedConfig = config;
                else if (securityType == WifiConfiguration.KeyMgmt.NONE)
                    selectedConfig = config;
            }
        }

        if (selectedConfig != null) {
            selectedConfig.priority = highestPriorityNumber + 1;
            mWifiManager.updateNetwork(selectedConfig);
            // mWifiManager.disconnect(); /* disconnect from whichever wifi you're connected to
            mWifiManager.enableNetwork(selectedConfig.networkId, true);
            mWifiManager.reconnect();
            Log.d(TAG, "Connection exists in past, enabling and connecting priority = " + highestPriorityNumber);
            return NetworkInfo.DetailedState.CONNECTING;
        }

        // Make new connection
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = quoted(ssid);
        config.priority = highestPriorityNumber + 1;
        config.status = WifiConfiguration.Status.ENABLED;
        if (securityType == WifiConfiguration.KeyMgmt.WPA_PSK) {
            config.preSharedKey = quoted(key);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        Log.d(TAG, "Attempting new wifi connection, setting priority number to, connecting " + config.priority);

        int netId = mWifiManager.addNetwork(config);
        // mWifiManager.disconnect();  disconnect from whichever wifi you're connected to
        mWifiManager.enableNetwork(netId, true);
        mWifiManager.reconnect(); // todo?
        return NetworkInfo.DetailedState.CONNECTING;
    }

*/
}