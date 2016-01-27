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
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.prey.managers.PreyConnectivityManager;

import android.*;
import android.Manifest;
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
import android.os.Debug;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

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
        hardware.setUuid(getUuid());
        hardware.setAndroidDeviceId(getAndroidDeviceId());
        hardware.setBiosVendor(Build.MANUFACTURER);
        hardware.setBiosVersion(mapData.get("Revision"));
        hardware.setMbVendor(Build.MANUFACTURER);
        //	hardware.setMbVersion(Build.BOOTLOADER );
        hardware.setMbModel(Build.BOARD);
        //hardware.setMbVersion(mbVersion);
        hardware.setCpuModel(mapData.get("Processor"));
        hardware.setCpuSpeed(String.valueOf(maxCPUFreqMHz()));
        hardware.setCpuCores(String.valueOf(getCpuCores()));
        hardware.setRamSize(String.valueOf(getMemoryRamSize()));
        // hardware.setRamModules(ramModules);
        hardware.setSerialNumber(getSerialNumber());

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
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
            try {
                ir.close();
            } catch (Exception e) {
            }
            try {
                fi.close();
            } catch (Exception e) {
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
            }
            try {
                ir.close();
            } catch (Exception e) {
            }
            try {
                fi.close();
            } catch (Exception e) {
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
            wifi.setMacAddress(wifiInfo.getMacAddress());
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

            }
            wifi.setSsid(ssid);

            for (int i = 0; listWifi != null && i < listWifi.size(); i++) {
                Wifi _wifi = listWifi.get(i);
                ssid = _wifi.getSsid();
                try {
                    ssid = ssid.replaceAll("\"", "");
                } catch (Exception e) {

                }
                if (ssid.equals(wifi.getSsid())) {
                    wifi.setSecurity(_wifi.getSecurity());
                    wifi.setSignalStrength(_wifi.getSignalStrength());
                    wifi.setChannel(_wifi.getChannel());
                    break;
                }
            }
        } catch (Exception e) {
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
                String[] data = aLine.split(":");
                try {
                    mapData.put(data[0].trim(), data[1].trim());
                } catch (Exception e) {
                }
            }
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
        }
        return mapData;
    }

    private int getCpuCores() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.availableProcessors();
    }

    private String getSerialNumber() {
        try{
            return getUuid();
        } catch(Exception e) {
            return "";
        }
    }


    public String getIPAddress() {
        String ip = "";
        DefaultHttpClient httpClient = null;
        HttpGet httpGet = null;
        HttpResponse httpResponse = null;
        InputStreamReader input = null;
        BufferedReader buffer = null;
        try {
            httpClient = new DefaultHttpClient();
            httpGet = new HttpGet("http://ifconfig.me/ip");
            httpResponse = httpClient.execute(httpGet);
            input = new InputStreamReader(httpResponse.getEntity().getContent());
            buffer = new BufferedReader(input);
            ip = buffer.readLine();
        } catch (Exception e) {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e1) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e1) {
                }
            }
        }
        return ip;
    }

    private static final int REQUEST_READ_PHONE_STATE_PERMISSION = 225;



    private String getAndroidDeviceId() {
        return android.provider.Settings.Secure.getString(ctx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }


    public int getSimState(){
        TelephonyManager tManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        int simState =-1;
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ctx.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    simState = tManager.getSimState();
                }else{
                    PreyLogger.i("___________ask for permission getSimState READ_PHONE_STATE");
                }
            }else {
                simState = tManager.getSimState();
            }
        }catch (Exception e){
            PreyLogger.e("Error getSimSerialNumber:"+e.getMessage(),e);
        }
        return simState;
    }


    private String getUuid() {
        String uuid="";
        TelephonyManager tManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ctx.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    uuid=tManager.getDeviceId();
                }else{
                    PreyLogger.i("___________ask for permission getDeviceId READ_PHONE_STATE");
                }
            }else {
                uuid = tManager.getDeviceId();
            }
        }catch (Exception e){
            PreyLogger.e("Error getUuid:"+e.getMessage(),e);
        }
        PreyLogger.d("uuid["+uuid+"]");
        return uuid;
    }

    public String getSimSerialNumber(){
        TelephonyManager tManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String simSerialNumber ="";
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ctx.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    simSerialNumber = tManager.getSimSerialNumber();
                }else{
                    PreyLogger.i("___________ask for permission getSimSerialNumber READ_PHONE_STATE");
                }
            }else {
                simSerialNumber = tManager.getSimSerialNumber();
            }
        }catch (Exception e){
            PreyLogger.e("Error getSimSerialNumber:"+e.getMessage(),e);
        }
        PreyLogger.d("simSerialNumber["+simSerialNumber+"]");
        return simSerialNumber;
    }


    public int getDataState(){
        TelephonyManager tManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        int dataState =-1;
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ctx.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    dataState = tManager.getDataState();
                }else{
                    PreyLogger.i("___________ask for permission getDataState READ_PHONE_STATE");
                }
            }else {
                dataState = tManager.getDataState();
            }
        }catch (Exception e){
            PreyLogger.e("Error getDataState:"+e.getMessage(),e);
        }
        return dataState;
    }


    public String getLine1Number(){
        TelephonyManager tManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String line1Number ="";
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ctx.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    line1Number = tManager.getLine1Number();
                }else{
                    PreyLogger.i("___________ask for permission getLine1Number READ_PHONE_STATE");
                }
            }else {
                line1Number = tManager.getLine1Number();
            }
        }catch (Exception e){
            PreyLogger.e("Error getDataState:"+e.getMessage(),e);
        }
        return line1Number;
    }


}

