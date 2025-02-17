/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat

import com.prey.backwardcompatibility.FroyoSupport
import com.prey.managers.PreyConnectivityManager
import com.prey.net.PreyWebServices

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Method

/**
 * Represents a Prey phone device.
 *
 * This class encapsulates the device's hardware and Wi-Fi information.
 */
class PreyPhone internal constructor(var context: Context) {

    var hardware: PreyHardware? = null
    private var listWifi: MutableList<PreyWifi>? = ArrayList<PreyWifi>()
    private var wifi: PreyWifi? = PreyWifi()

    /**
     * Initializes the Prey phone device.
     *
     * This method updates the device's hardware information, available Wi-Fi networks, and current Wi-Fi connection.
     */
    private fun init() {
        updateHardware()
        updateListWifi()
        updateWifi()
    }

    fun setWifi(wifi: PreyWifi?) {
        this.wifi = wifi
    }

    fun getWifi(): PreyWifi? {
        return wifi
    }

    fun updateHardware() {
        val mapData = processorData()
        hardware = PreyHardware()
        hardware!!.setAndroidDeviceId(getAndroidDeviceId());
        hardware!!.setBiosVendor(Build.MANUFACTURER);
        hardware!!.setBiosVersion(mapData.get("Revision"));
        hardware!!.setMbVendor(Build.MANUFACTURER);
        hardware!!.setMbModel(Build.BOARD);
        hardware!!.setCpuModel(mapData.get("Processor"));
        try {
            hardware!!.setCpuSpeed(maxCPUFreqMHz().toString())
        } catch (e: java.lang.Exception) {
            PreyLogger.d("Error setCpuSpeed:${e.message}")
        }
        hardware!!.setCpuCores(java.lang.String.valueOf(getCpuCores()))
        hardware!!.setRamSize(java.lang.String.valueOf(getMemoryRamSize()))
        hardware!!.setSerialNumber(getSerialNumber())
        hardware!!.setUuid(FroyoSupport.getInstance(context).getEnrollmentSpecificId())
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalMemory = totalMemory()
        val freeMemory = memoryInfo.availMem / 1048576L
        val usageMemory = totalMemory - freeMemory
        hardware!!.setTotalMemory(totalMemory)
        hardware!!.setFreeMemory(totalMemory)
        hardware!!.setBusyMemory(usageMemory)
    }

    fun getCpuCores(): Int {
        val runtime = Runtime.getRuntime()
        return runtime.availableProcessors()
    }

    fun getMemoryRamSize(): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(mInfo)
        return (mInfo.threshold shr 20)
    }

    fun totalMemory(): Long {
        var line = ""
        var file: File? = null
        var fi: FileInputStream? = null
        var ir: InputStreamReader? = null
        var br: BufferedReader? = null
        var totalMemory: Long = 0
        try {
            file = File("/proc/meminfo")
            fi = FileInputStream(file)
            ir = InputStreamReader(fi)
            br = BufferedReader(ir)
            while ((br.readLine().also { line = it }) != null) {
                if (line.indexOf("MemTotal") >= 0) {
                    line = line.replace("MemTotal", "")
                    line = line.replace(":", "")
                    line = line.replace("kB", "")
                    line = line.trim { it <= ' ' }
                    break
                }
            }
            totalMemory = line.toLong() / 1024
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        } finally {
            try {
                br!!.close()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            try {
                ir!!.close()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            try {
                fi!!.close()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
        }
        return totalMemory
    }

    fun maxCPUFreqMHz(): Long {
        var line: String? = ""
        var file: File? = null
        var fi: FileInputStream? = null
        var ir: InputStreamReader? = null
        var br: BufferedReader? = null
        var cpuMaxFreq: Long = 0
        try {
            file = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            fi = FileInputStream(file)
            ir = InputStreamReader(fi)
            br = BufferedReader(ir)
            while ((br.readLine().also { line = it }) != null) {
                if (line != null && "" != line) {
                    break
                }
            }
            cpuMaxFreq = line!!.toLong() / 1000
        } catch (e: Exception) {
        } finally {
            try {
                br!!.close()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            try {
                ir!!.close()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            try {
                fi!!.close()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
        }
        return cpuMaxFreq
    }

    fun getAndroidDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun updateWifi() {
        try {
            val wifiMgr = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiMgr.connectionInfo
            wifi!!.setWifiEnabled(wifiMgr.isWifiEnabled)
            val ipAddress = wifiInfo.ipAddress
            wifi!!.setIpAddress(formatterIp(ipAddress))
            val dhcpInfo = wifiMgr.dhcpInfo
            wifi!!.setNetmask(formatterIp(dhcpInfo.netmask))
            wifi!!.setGatewayIp(formatterIp(dhcpInfo.serverAddress))
            wifi!!.setMacAddress(wifiInfo.bssid)
            if (ipAddress != 0) {
                wifi!!.setInterfaceType("Wireless")
            } else {
                if (PreyConnectivityManager.getInstance().isMobileConnected(context)) {
                    wifi!!.setInterfaceType("Mobile")
                } else {
                    wifi!!.setInterfaceType("")
                }
            }
            wifi!!.setName("eth0")
            var ssid = wifiInfo.ssid
            try {
                ssid = ssid.replace("\"".toRegex(), "")
            } catch (e: java.lang.Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            wifi!!.setSsid(ssid)
            var i = 0
            while (listWifi != null && i < listWifi!!.size) {
                val _wifi = listWifi!![i]
                ssid = _wifi.getSsid()
                try {
                    ssid = ssid.replace("\"".toRegex(), "")
                } catch (e: java.lang.Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
                if (ssid == wifi!!.getSsid()) {
                    wifi!!.setSecurity(_wifi.getSecurity())
                    wifi!!.setSignalStrength(_wifi.getSignalStrength())
                    wifi!!.setChannel(_wifi.getChannel())
                    break
                }
                i++
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    private fun formatterIp(ipAddress: Int): String {
        return "${(ipAddress and 0xff)}.${(ipAddress shr 8 and 0xff)}.${(ipAddress shr 16 and 0xff)}.${(ipAddress shr 24 and 0xff)}"
    }

    private fun updateListWifi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            val wifiMgr = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val listScanResults = wifiMgr.scanResults
            var i = 0
            while (listScanResults != null && i < listScanResults.size) {
                val scan = listScanResults[i]
                val _wifi = PreyWifi()
                _wifi.setSsid(scan.SSID)
                _wifi.setMacAddress(scan.BSSID)
                _wifi.setSecurity(scan.capabilities)
                _wifi.setSignalStrength(java.lang.String.valueOf(scan.level))
                _wifi.setChannel(getChannelFromFrequency(scan.frequency).toString())
                listWifi!!.add(_wifi)
                i++
            }
        }
    }

    private fun getChannelFromFrequency(frequency: Int): Int {
        return channelsFrequency.indexOf(frequency)
    }

    fun getListWifi(): List<PreyWifi>? {
        return listWifi
    }


    fun processorData(): MutableMap<String, String?> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(mInfo)
        val args = arrayOf("/system/bin/cat", "/proc/cpuinfo")
        val pb = ProcessBuilder(*args)
        val process: Process
        val mapData: MutableMap<String, String?> = HashMap()
        try {
            process = pb.start()
            val input = process.inputStream
            val br = BufferedReader(InputStreamReader(input))
            var aLine: String
            while ((br.readLine().also { aLine = it }) != null) {
                if ("" != aLine) {
                    try {
                        val data: List<String> = aLine.split(":")
                        mapData[data[0].trim()] = data[1].trim()
                    } catch (e: Exception) {
                        PreyLogger.e("Error:${e.message}", e)
                    }
                } else {
                    break
                }
            }

        } catch (e: IOException) {
            PreyLogger.e("Error:${e.message}", e)
        }
        return mapData
    }

    fun getIpAddress(): String {
        var ip = ""
        try {
            ip = PreyWebServices.getInstance().getIPAddress(context)!!
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        return ip
    }

    /**
     * Checks if the airplane mode is currently enabled on the device.
     *
     * @param context the application context
     * @return true if airplane mode is on, false otherwise
     */
    fun isAirplaneModeOn(context: Context): Boolean {
        // Get the current airplane mode setting from the system settings
        val isAirplaneModeOn = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) == 1
        // Log the result for debugging purposes
        PreyLogger.d("isAirplaneModeOn: ${isAirplaneModeOn}")
        // Return the result
        return isAirplaneModeOn
    }

    init {
        init()
    }

    fun getDataState(): Int {
        val tManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var dataState = -1
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    dataState = tManager.dataState
                }
            } else {
                dataState = tManager.dataState
            }
        } catch (e: Exception) {
            PreyLogger.e("Error getDataState:${e.message}", e)
        }
        return dataState
    }


    @SuppressLint("MissingPermission")
    fun getNetworkClass(context: Context): String? {
        try {
            val mTelephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val networkType = mTelephonyManager.networkType
            return when (networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                else -> null
            }
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Retrieves the device's serial number.
     *
     * This method attempts to retrieve the serial number from various system properties.
     * If all attempts fail, it falls back to using the Build.SERIAL property.
     *
     * @return the device's serial number, or null if it could not be retrieved
     */
    fun getSerialNumber(): String? {
        // Initialize the serial number to null
        var serialNumber: String? = null
        try {
            // Get the SystemProperties class
            val c = Class.forName("android.os.SystemProperties")
            // Get the get() method of the SystemProperties class
            val getMethod = c.getMethod("get", String::class.java)
            // Attempt to retrieve the serial number from various system properties
            serialNumber =
                getSerialNumberFromProperty(getMethod, "gsm.sn1") // GSM serial number
            if (serialNumber == null) {
                serialNumber = getSerialNumberFromProperty(
                    getMethod,
                    "ril.serialnumber"
                ) // RIL serial number
            }
            if (serialNumber == null) {
                serialNumber = getSerialNumberFromProperty(
                    getMethod,
                    "ro.serialno"
                ) // Serial number from ro.serialno property
            }
            if (serialNumber == null) {
                serialNumber = getSerialNumberFromProperty(
                    getMethod,
                    "sys.serialnumber"
                ) // Serial number from sys.serialnumber property
            }
            if (serialNumber == null) {
                // If all else fails, use the Build.SERIAL property
                serialNumber = Build.SERIAL
            }
        } catch (e: Exception) {
            PreyLogger.e("Error getSerialNumber:${e.message}", e)
            serialNumber = null
        }
        // Return the retrieved serial number, or null if it could not be retrieved
        return serialNumber
    }

    /**
     * Retrieves the value of the specified system property.
     *
     * @param getMethod    the method used to retrieve the system property value
     * @param propertyName the name of the system property to retrieve
     * @return the value of the system property, or null if it could not be retrieved
     * @throws Exception if an error occurs while retrieving the system property value
     */
    @Throws(Exception::class)
    private fun getSerialNumberFromProperty(getMethod: Method, propertyName: String): String {
        // Invoke the getMethod with the propertyName as an argument to retrieve the system property value
        return getMethod.invoke(null, propertyName) as String
    }

    companion object {
        var TAG: String = "memory"

        private var instance: PreyPhone? = null

        @Synchronized
        fun getInstance(context: Context): PreyPhone {
            if (instance == null) {
                instance = PreyPhone(context)
            }
            return instance!!
        }

        private val channelsFrequency: List<Int> = ArrayList(
            mutableListOf(
                0,
                2412,
                2417,
                2422,
                2427,
                2432,
                2437,
                2442,
                2447,
                2452,
                2457,
                2462,
                2467,
                2472,
                2484
            )
        )

        private const val REQUEST_READ_PHONE_STATE_PERMISSION = 225
    }
}