package com.prey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
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

 

	private void init() {
		updateHardware();
		updateWifi();
		updateListWifi();
		update3g();
	}

	private void update3g() {
		
		
	}
	private void updateHardware() {
		Map<String,String> mapData=getProcessorData();
		hardware = new Hardware();
		hardware.setUuid(Build.ID );
		hardware.setBiosVendor(Build.MANUFACTURER );
		hardware.setBiosVersion(mapData.get("Revision"));
		hardware.setMbVendor(Build.MANUFACTURER );
		//hardware.setMbVersion(Build.BOOTLOADER );
		hardware.setMbModel( Build.BOARD);
		//hardware.setMbVersion(mbVersion);
		hardware.setCpuModel(mapData.get("Processor"));
		hardware.setCpuSpeed(String.valueOf(maxCPUFreqMHz()));
		hardware.setCpuCores(String.valueOf(getCpuCores()));
		hardware.setRamSize(String.valueOf(getMemoryRamSize()));
		// hardware.setRamModules(ramModules);
		hardware.setSerialNumber(getSerialNumber());

	}
	
	private void updateWifi(){
		wifi=new Wifi();
		WifiManager wifiMgr = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo= wifiMgr.getConnectionInfo();
		int ipAddress=wifiInfo.getIpAddress();
		wifi.setIpAddress(formatterIp(ipAddress));
		wifi.setMacAddress(wifiInfo.getMacAddress());
		DhcpInfo dhcpInfo= wifiMgr.getDhcpInfo();
		wifi.setNetmask(formatterIp(dhcpInfo.netmask));
		wifi.setGatewayIp(formatterIp(dhcpInfo.serverAddress));
		if(ipAddress!=0)
			wifi.setInterfaceType("Wireless");
		else
			wifi.setInterfaceType("3G");
		wifi.setName("eth0");
		    	
	}
	
 

	

	private String formatterIp(int ipAddress){
		return String.format("%d.%d.%d.%d", 
				(ipAddress & 0xff), 
				(ipAddress >> 8 & 0xff), 
				(ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));
	}
	private void updateListWifi() {
		listWifi = new ArrayList<PreyPhone.Wifi>();
		WifiManager wifiMgr = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		List<ScanResult> listScanResults = wifiMgr.getScanResults();		 
		for (int i=0;listScanResults!=null&&i<  listScanResults.size();i++) {
			ScanResult scan=listScanResults.get(i);
			Wifi _wifi = new Wifi();
			_wifi.setSsid(scan.SSID);
			_wifi.setMacAddress(scan.BSSID);
			_wifi.setSecurity(scan.capabilities);
			_wifi.setSignalStrength(String.valueOf(scan.level));
			_wifi.setChannel(String.valueOf(getChannelFromFrequency(scan.frequency)));
			listWifi.add(_wifi);

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
	

	public  Wifi  getWifi() {
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

	
	/* maximum speeds.
	 *
	 * @return cpu frequency in MHz
	 */
	@SuppressWarnings("resource")
	private int maxCPUFreqMHz() {

	    int maxFreq = -1;
	    RandomAccessFile reader =null;
	    try {

	        reader = new RandomAccessFile( "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state", "r" );

	        boolean done = false;
	        while ( ! done ) {
	            String line = reader.readLine();
	            if ( null == line ) {
	                done = true;
	                break;
	            }
	            String[] splits = line.split( "\\s+" );
	            assert ( splits.length == 2 );
	            int timeInState = Integer.parseInt( splits[1] );
	            if ( timeInState > 0 ) {
	                int freq = Integer.parseInt( splits[0] ) / 1000;
	                if ( freq > maxFreq ) {
	                    maxFreq = freq;
	                }
	            }
	        }

	    } catch ( IOException ex ) {

	    }

	    return maxFreq;
	}
	
	private long getMemoryRamSize(){
		ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(mInfo);
		return (mInfo.threshold >> 20);
	}
	
	private Map<String,String> getProcessorData(){
		ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(mInfo);

		String[] args = { "/system/bin/cat", "/proc/cpuinfo" };
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
	
	private int getCpuCores(){
		Runtime runtime = Runtime.getRuntime();
		return runtime.availableProcessors();
	}
	private String getSerialNumber(){
		TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}
}
