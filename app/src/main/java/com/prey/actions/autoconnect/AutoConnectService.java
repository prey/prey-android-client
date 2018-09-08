package com.prey.actions.autoconnect;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;
import com.prey.net.UtilConnection;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AutoConnectService extends IntentService {
    private WifiManager mWifiManager;
    public AutoConnectService() {
        super("AutoConnectService");
    }

    public AutoConnectService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        run(getApplicationContext());
        stopSelf();
    }

    public void run(Context ctx) {
        PreyLogger.d("AutoConnect Service run");
        boolean cerrar = false;

        boolean haveNetwork = haveNetworkConnection(ctx);

        if (haveNetwork) {
            JSONObject jsnobject = null;
            try {
                jsnobject = PreyWebServices.getInstance().getStatus(ctx);
            } catch (Exception e) {
            }

            if (jsnobject != null)
                PreyLogger.i("AutoConnect Service jsnobject:" + jsnobject.toString());
            else
                PreyLogger.i("AutoConnect Service jsnobject nulo");

            if (jsnobject == null) {
                PreyLogger.i("AutoConnect Service no tiene haveNetwork");
                haveNetwork = false;
            }
        }


        PreyLogger.i("AutoConnect Service haveNetwork:" + haveNetwork);
        if (!haveNetwork) {
            boolean wifiConnected = false;
            boolean mobileConnected = false;
            updateConnectedFlags(ctx);
            PreyLogger.i("AutoConnect Service wifiConnected:" + wifiConnected);


            mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            PreyLogger.i("AutoConnect Service mWifiManager.isWifiEnabled():" + mWifiManager.isWifiEnabled());

            if (!mWifiManager.isWifiEnabled()) {

                mWifiManager.setWifiEnabled(true);
                try {
                    Thread.sleep(4000);
                } catch (Exception e) {
                }
                PreyLogger.i("AutoConnect Service mWifiManager.isWifiEnabled():" + mWifiManager.isWifiEnabled());
                cerrar = true;
            }
            List<ScanResult> results = null;
            int j = 0;

            do {
                mWifiManager.reconnect();
                mWifiManager.startScan();
                results = mWifiManager.getScanResults();
                PreyLogger.i("AutoConnect Service results intento[" + j + "] tamanio" + results.size());
                j++;
                if (results.size() == 0) {
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                    }
                }
            } while (results.size() == 0 && j < 20);



            List<String> openList=new ArrayList<>();
            for (int i = 0; results != null && i < results.size(); i++) {
                ScanResult scan = results.get(i);
                final String ssid = scan.SSID;
                // PreyLogger.i("AutoConnect Service ["+i+"] SSID:" + scan.SSID + " capabilities:" + scan.capabilities + " ");
                AutoConnectBlacklist.getInstance().print();
                if ("[ESS]".equals(scan.capabilities) && !AutoConnectBlacklist.getInstance().contains(ssid)&& !ssid.equals("Prey-Guest")) {
                    PreyLogger.i("AutoConnect Service SSID:" + ssid + " capabilities:" + scan.capabilities + " ");
                    if(!openList.contains(ssid))
                        openList.add(ssid);
                }
            }


            Comparator<String> comp = new Comparator<String>() {
                @Override
                public int compare(String a, String b) {
                    return a.compareTo(b);
                }
            };


            Collections.sort(openList, comp);
            PreyLogger.i("AutoConnect Service openList size:" + (openList==null?0:openList.size()));
            for (int i = 0; openList != null && i < openList.size(); i++) {
                String ssid = openList.get(i);
                int networkId = connect(ctx, ssid);
                try {
                    Thread.sleep(4000);
                } catch (Exception e) {
                }
                haveNetwork = haveNetworkConnection(ctx);
                PreyLogger.i("AutoConnect Service haveNetwork["+i+"][" + ssid + "]:" + haveNetwork);
                if (haveNetwork) {
                    JSONObject jsnobject = null;
                    try {
                        String device=PreyConfig.getPreyConfig(ctx).getDeviceId();
                        String uri = "https://solid.preyproject.com/api/v2/devices/"+device+"/status.json";
                        URL url = new URL(uri);
                        PreyLogger.i("AutoConnect Service url["+i+"][" + ssid + "]:" + url);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(3000);
                        connection.addRequestProperty("Authorization", UtilConnection.getAuthorization(PreyConfig.getPreyConfig(ctx)));
                        int responseCode = connection.getResponseCode();

                        PreyLogger.i("AutoConnect Service responseCode["+i+"][" + ssid + "]:" + responseCode);
                        PreyHttpResponse response = UtilConnection.convertPreyHttpResponse(responseCode, connection);
                        String responseAsString = null;
                        if (response != null) {
                            responseAsString = response.getResponseAsString();
                            PreyLogger.i("AutoConnect Service responseAsString["+i+"][" + ssid + "]:" + responseAsString);
                        }
                        if (responseAsString != null) {
                            jsnobject = new JSONObject(responseAsString);
                        }

                        PreyLogger.i("AutoConnect Service jsnobject["+i+"][" + ssid + "]:" + jsnobject);

                    } catch (Exception e) {
                        PreyLogger.i("AutoConnect Service error["+i+"][" + ssid + "]:" + e.getMessage());
                    }

                    if (jsnobject != null) {

                        break;
                    } else {
                        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
                        wifiManager.removeNetwork(networkId);

                        PreyLogger.i("AutoConnect Service removeNetwork ["+i+"][" + ssid + "]:" + networkId);
                        AutoConnectBlacklist.getInstance().add(ssid);
                    }
                }
                //

            }
            if (cerrar) {
                PreyLogger.i("AutoConnect Service mWifiManager.isWifiEnabled():" + mWifiManager.isWifiEnabled());
                //mWifiManager.setWifiEnabled(false);
                PreyLogger.i("AutoConnect Service mWifiManager.isWifiEnabled():" + mWifiManager.isWifiEnabled());
            }
        }

    }


    public int connect(Context ctx,String networkSSID) {
        PreyLogger.i("AutoConnect Service connect:" + networkSSID);

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

        config.preSharedKey  = "\"p8hHxCw8jxrq\"";

        int networkId = wifiManager.addNetwork(config);
        PreyLogger.i("AutoConnect Service networkSSID["+networkSSID+"] networkId:" + networkId);
        if (networkId != -1) {


            boolean isDisconnected = wifiManager.disconnect();
            PreyLogger.i("isDisconnected : " + isDisconnected);

            boolean isEnabled = wifiManager.enableNetwork(networkId, true);
            PreyLogger.i("isEnabled : " + isEnabled);

            boolean isReconnected = wifiManager.reconnect();

        }
        return networkId;

    }

    private boolean haveNetworkConnection(Context ctx) {

        boolean haveConnectedMobile = false;
        boolean haveConnectedWifi = false;
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    boolean wifiConnected=false;
    boolean mobileConnected=false;

    public void updateConnectedFlags(Context ctx) {
        ConnectivityManager connMgr = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
    }




}
