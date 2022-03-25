/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.prey.managers.PreyConnectivityManager;
import com.prey.net.PreyWebServices;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

public class PreyPhone {

    private Context ctx;
    private Hardware hardware;
    private List<Wifi> listWifi;
    private Wifi wifi;

    public PreyPhone(Context ctx) {
        this.ctx = ctx;
        init();
    }

    public static String TAG = "memory";

    private void init() {
        updateHardware();
        updateListWifi();
        updateWifi();
        update3g();
    }

    private void update3g() {
    }

    private void updateHardware() {
        Map<String, String> mapData = getProcessorData();
        hardware = new Hardware();
        hardware.setAndroidDeviceId(getAndroidDeviceId());
        hardware.setBiosVendor(Build.MANUFACTURER);
        hardware.setBiosVersion(mapData.get("Revision"));
        hardware.setMbVendor(Build.MANUFACTURER);
        hardware.setMbModel(Build.BOARD);
        hardware.setCpuModel(mapData.get("Processor"));
        try {
            hardware.setCpuSpeed(String.valueOf(maxCPUFreqMHz()));
        } catch (Exception e) {
            PreyLogger.d(String.format("Error setCpuSpeed:%s", e.getMessage()));
        }
        hardware.setCpuCores(String.valueOf(getCpuCores()));
        hardware.setRamSize(String.valueOf(getMemoryRamSize()));
        initMemory();
    }

    @TargetApi(16)
    private void initMemory() {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long totalMemory = totalMemory();
        long freeMemory = memoryInfo.availMem / 1048576L;
        long usageMemory = totalMemory - freeMemory;
        hardware.setTotalMemory(totalMemory);
        hardware.setFreeMemory(totalMemory);
        hardware.setBusyMemory(usageMemory);
    }

    public long totalMemory() {
        String line = "";
        File file = null;
        FileInputStream fi = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        long totalMemory = 0;
        try {
            file = new File("/proc/meminfo");
            fi = new FileInputStream(file);
            ir = new InputStreamReader(fi);
            br = new BufferedReader(ir);
            while ((line = br.readLine()) != null) {
                if (line.indexOf("MemTotal") >= 0) {
                    line = line.replace("MemTotal", "");
                    line = line.replace(":", "");
                    line = line.replace("kB", "");
                    line = line.trim();
                    break;
                }
            }
            totalMemory = Long.parseLong(line) / 1024;
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            try {
                ir.close();
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            try {
                fi.close();
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
        }
        return totalMemory;
    }

    public long maxCPUFreqMHz() {
        String line = "";
        File file = null;
        FileInputStream fi = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        long cpuMaxFreq = 0;
        try {
            file = new File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
            fi = new FileInputStream(file);
            ir = new InputStreamReader(fi);
            br = new BufferedReader(ir);
            while ((line = br.readLine()) != null) {
                if (line != null && !"".equals(line)) {
                    break;
                }
            }
            cpuMaxFreq = Long.parseLong(line) / 1000;
        } catch (Exception e) {
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            try {
                ir.close();
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            try {
                fi.close();
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
        }
        return cpuMaxFreq;
    }

    private void updateWifi() {
        wifi = new Wifi();
        try {
            WifiManager wifiMgr = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            wifi.setWifiEnabled(wifiMgr.isWifiEnabled());
            int ipAddress = wifiInfo.getIpAddress();
            wifi.setIpAddress(formatterIp(ipAddress));
            DhcpInfo dhcpInfo = wifiMgr.getDhcpInfo();
            wifi.setNetmask(formatterIp(dhcpInfo.netmask));
            wifi.setGatewayIp(formatterIp(dhcpInfo.serverAddress));
            if (ipAddress != 0) {
                wifi.setInterfaceType("Wireless");
            } else {
                if (PreyConnectivityManager.getInstance(ctx).isMobileConnected()) {
                    wifi.setInterfaceType("Mobile");
                } else {
                    wifi.setInterfaceType("");
                }
            }
            wifi.setName("eth0");
            String ssid = wifiInfo.getSSID();
            try {
                ssid = ssid.replaceAll("\"", "");
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            wifi.setSsid(ssid);
            for (int i = 0; listWifi != null && i < listWifi.size(); i++) {
                Wifi _wifi = listWifi.get(i);
                ssid = _wifi.getSsid();
                try {
                    ssid = ssid.replaceAll("\"", "");
                } catch (Exception e) {
                    PreyLogger.e("Error:"+e.getMessage(),e);
                }
                if (ssid.equals(wifi.getSsid())) {
                    wifi.setSecurity(_wifi.getSecurity());
                    wifi.setSignalStrength(_wifi.getSignalStrength());
                    wifi.setChannel(_wifi.getChannel());
                    break;
                }
            }
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
    }

    private String formatterIp(int ipAddress) {
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
    }

    private void updateListWifi() {
        listWifi = new ArrayList<PreyPhone.Wifi>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            WifiManager wifiMgr = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> listScanResults = wifiMgr.getScanResults();
            for (int i = 0; listScanResults != null && i < listScanResults.size(); i++) {
                ScanResult scan = listScanResults.get(i);
                Wifi _wifi = new Wifi();
                _wifi.setSsid(scan.SSID);
                _wifi.setMacAddress(scan.BSSID);
                _wifi.setSecurity(scan.capabilities);
                _wifi.setSignalStrength(String.valueOf(scan.level));
                _wifi.setChannel(String.valueOf(getChannelFromFrequency(scan.frequency)));
                listWifi.add(_wifi);
            }
        }
    }

    private int getChannelFromFrequency(int frequency) {
        return channelsFrequency.indexOf(Integer.valueOf(frequency));
    }

    public Hardware getHardware() {
        return hardware;
    }

    public List<Wifi> getListWifi() {
        return listWifi;
    }


    public Wifi getWifi() {
        return wifi;
    }

    private final static List<Integer> channelsFrequency = new ArrayList<Integer>(Arrays.asList(0, 2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447, 2452, 2457, 2462, 2467, 2472, 2484));

    public class Hardware {
        private String uuid;
        private String biosVendor;
        private String biosVersion;
        private String mbVendor;
        private String mbSerial;
        private String mbModel;
        private String mbVersion;
        private String cpuModel;
        private String cpuSpeed;
        private String cpuCores;
        private String ramSize;
        private String ramModules;
        private String serialNumber;
        private long totalMemory;
        private long freeMemory;
        private long busyMemory;
        private String androidDeviceId;

        public long getTotalMemory() {
            return totalMemory;
        }

        public void setTotalMemory(long totalMemory) {
            this.totalMemory = totalMemory;
        }

        public long getFreeMemory() {
            return freeMemory;
        }

        public void setFreeMemory(long freeMemory) {
            this.freeMemory = freeMemory;
        }

        public long getBusyMemory() {
            return busyMemory;
        }

        public void setBusyMemory(long busyMemory) {
            this.busyMemory = busyMemory;
        }

        public String getRamSize() {
            return ramSize;
        }

        public void setRamSize(String ramSize) {
            this.ramSize = ramSize;
        }

        public String getRamModules() {
            return ramModules;
        }

        public void setRamModules(String ramModules) {
            this.ramModules = ramModules;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getBiosVendor() {
            return biosVendor;
        }

        public void setBiosVendor(String biosVendor) {
            this.biosVendor = biosVendor;
        }

        public String getBiosVersion() {
            return biosVersion;
        }

        public void setBiosVersion(String biosVersion) {
            this.biosVersion = biosVersion;
        }

        public String getMbVendor() {
            return mbVendor;
        }

        public void setMbVendor(String mbVendor) {
            this.mbVendor = mbVendor;
        }

        public String getMbSerial() {
            return mbSerial;
        }

        public void setMbSerial(String mbSerial) {
            this.mbSerial = mbSerial;
        }

        public String getMbModel() {
            return mbModel;
        }

        public void setMbModel(String mbModel) {
            this.mbModel = mbModel;
        }

        public String getMbVersion() {
            return mbVersion;
        }

        public void setMbVersion(String mbVersion) {
            this.mbVersion = mbVersion;
        }

        public String getCpuModel() {
            return cpuModel;
        }

        public void setCpuModel(String cpuModel) {
            this.cpuModel = cpuModel;
        }

        public String getCpuSpeed() {
            return cpuSpeed;
        }

        public void setCpuSpeed(String cpuSpeed) {
            this.cpuSpeed = cpuSpeed;
        }

        public String getCpuCores() {
            return cpuCores;
        }

        public void setCpuCores(String cpuCores) {
            this.cpuCores = cpuCores;
        }

        public void setAndroidDeviceId(String androidDeviceId) { this.androidDeviceId = androidDeviceId; }

        public String getAndroidDeviceId() { return androidDeviceId; }

    }

    public class Wifi {
        private String name;
        private String interfaceType;
        private String model;
        private String vendor;
        private String ipAddress;
        private String gatewayIp;
        private String netmask;
        private String macAddress;
        private String ssid;
        private String signalStrength;
        private String channel;
        private String security;
        private boolean wifiEnabled;

        public boolean isWifiEnabled() {
            return wifiEnabled;
        }

        public void setWifiEnabled(boolean wifiEnabled) {
            this.wifiEnabled = wifiEnabled;
        }

        public String getSsid() {
            return ssid;
        }

        public void setSsid(String ssid) {
            this.ssid = ssid;
        }

        public String getSignalStrength() {
            return signalStrength;
        }

        public void setSignalStrength(String signalStrength) {
            this.signalStrength = signalStrength;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getSecurity() {
            return security;
        }

        public void setSecurity(String security) {
            this.security = security;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getInterfaceType() {
            return interfaceType;
        }

        public void setInterfaceType(String interfaceType) {
            this.interfaceType = interfaceType;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getVendor() {
            return vendor;
        }

        public void setVendor(String vendor) {
            this.vendor = vendor;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getGatewayIp() {
            return gatewayIp;
        }

        public void setGatewayIp(String gatewayIp) {
            this.gatewayIp = gatewayIp;
        }

        public String getNetmask() {
            return netmask;
        }

        public void setNetmask(String netmask) {
            this.netmask = netmask;
        }

        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }

    }

    private long getMemoryRamSize() {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mInfo);
        return (mInfo.threshold >> 20);
    }

    private Map<String, String> getProcessorData() {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mInfo);
        String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
        ProcessBuilder pb = new ProcessBuilder(args);
        Process process;
        Map<String, String> mapData = new HashMap<String, String>();
        try {
            process = pb.start();
            InputStream in = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String aLine;
            while ((aLine = br.readLine()) != null) {
                if (!"".equals(aLine)) {
                    try {
                        String[] data = aLine.split(":");
                        mapData.put(data[0].trim(), data[1].trim());
                    } catch (Exception e) {
                        PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
                    }
                }
            }
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        return mapData;
    }

    private int getCpuCores() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.availableProcessors();
    }

    public String getIPAddress() {
        String ip = "";
        try {
            ip = PreyWebServices.getInstance().getIPAddress(ctx);
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        return ip;
    }

    private static final int REQUEST_READ_PHONE_STATE_PERMISSION = 225;

    private String getAndroidDeviceId() {
        return android.provider.Settings.Secure.getString(ctx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

    public int getDataState(){
        TelephonyManager tManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        int dataState =-1;
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ctx.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    dataState = tManager.getDataState();
                }
            }else {
                dataState = tManager.getDataState();
            }
        }catch (Exception e){
            PreyLogger.e("Error getDataState:"+e.getMessage(),e);
        }
        return dataState;
    }

    @SuppressLint("MissingPermission")
    public static String getNetworkClass(Context ctx) {
        try{
            TelephonyManager mTelephonyManager = (TelephonyManager)
                    ctx.getSystemService(Context.TELEPHONY_SERVICE);
            int networkType = mTelephonyManager.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "4G";
                default:
                    return null;
            }
        }catch (Exception e){
            return null;
        }
    }

}