/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.kotlin

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
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

class PreyPhone(private val ctx: Context) {
    var hardware: Hardware? = null
        private set
    private var listWifi: MutableList<Wifi>? = null
    var wifi: Wifi? = null
        private set

    private fun init() {
        updateHardware()
        updateListWifi()
        updateWifi()
        update3g()
    }

    private fun update3g() {
    }

    private fun updateHardware() {
        val mapData = processorData
        hardware = Hardware()
        hardware!!.androidDeviceId = getAndroidDeviceId()
        hardware!!.biosVendor = Build.MANUFACTURER
        hardware!!.biosVersion = mapData["Revision"]
        hardware!!.mbVendor = Build.MANUFACTURER
        hardware!!.mbModel = Build.BOARD
        hardware!!.cpuModel = mapData["Processor"]
        try {
            hardware!!.cpuSpeed = maxCPUFreqMHz().toString()
        } catch (e: Exception) {
            PreyLogger.d(String.format("Error setCpuSpeed:%s", e.message))
        }
        hardware!!.cpuCores = cpuCores.toString()
        hardware!!.ramSize = memoryRamSize.toString()
        hardware!!.serialNumber = serialNumber
        hardware!!.uuid = FroyoSupport.getInstance(ctx).enrollmentSpecificId
        initMemory()
    }

    @TargetApi(16)
    private fun initMemory() {
        val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalMemory = totalMemory()
        val freeMemory = memoryInfo.availMem / 1048576L
        val usageMemory = totalMemory - freeMemory
        hardware!!.totalMemory = totalMemory
        hardware!!.freeMemory = totalMemory
        hardware!!.busyMemory = usageMemory
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
        return Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun updateWifi() {
        wifi = Wifi()
        try {
            val wifiMgr = ctx.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiMgr.connectionInfo
            wifi!!.isWifiEnabled = wifiMgr.isWifiEnabled
            val ipAddress = wifiInfo.ipAddress
            wifi!!.ipAddress = formatterIp(ipAddress)
            val dhcpInfo = wifiMgr.dhcpInfo
            wifi!!.netmask = formatterIp(dhcpInfo.netmask)
            wifi!!.gatewayIp = formatterIp(dhcpInfo.serverAddress)
            if (ipAddress != 0) {
                wifi!!.interfaceType = "Wireless"
            } else {
                if (PreyConnectivityManager.getInstance(ctx).isMobileConnected) {
                    wifi!!.interfaceType = "Mobile"
                } else {
                    wifi!!.interfaceType = ""
                }
            }
            wifi!!.name = "eth0"
            var ssid = wifiInfo.ssid
            try {
                ssid = ssid!!.replace("\"".toRegex(), "")
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            wifi!!.ssid = ssid
            var i = 0
            while (listWifi != null && i < listWifi!!.size) {
                val _wifi = listWifi!![i]
                ssid = _wifi.ssid
                try {
                    ssid = ssid!!.replace("\"".toRegex(), "")
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
                if (ssid == wifi!!.ssid) {
                    wifi!!.security = _wifi.security
                    wifi!!.signalStrength = _wifi.signalStrength
                    wifi!!.channel = _wifi.channel
                    break
                }
                i++
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    private fun formatterIp(ipAddress: Int): String {
        return String.format(
            "%d.%d.%d.%d",
            (ipAddress and 0xff),
            (ipAddress shr 8 and 0xff),
            (ipAddress shr 16 and 0xff),
            (ipAddress shr 24 and 0xff)
        )
    }

    private fun updateListWifi() {
        listWifi = ArrayList()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            val wifiMgr = ctx.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val listScanResults = wifiMgr.scanResults
            var i = 0
            while (listScanResults != null && i < listScanResults.size) {
                val scan = listScanResults[i]
                val _wifi: Wifi = Wifi()
                _wifi.ssid = scan.SSID
                _wifi.macAddress = scan.BSSID
                _wifi.security = scan.capabilities
                _wifi.signalStrength = scan.level.toString()
                _wifi.channel = getChannelFromFrequency(scan.frequency).toString()
                (listWifi as ArrayList<Wifi>).add(_wifi)
                i++
            }
        }
    }

    private fun getChannelFromFrequency(frequency: Int): Int {
        return channelsFrequency.indexOf(frequency)
    }

    fun getListWifi(): List<Wifi>? {
        return listWifi
    }


    inner class Hardware {
        var uuid: String? = null
        var biosVendor: String? = null
        var biosVersion: String? = null
        var mbVendor: String? = null
        var mbSerial: String? = null
        var mbModel: String? = null
        var mbVersion: String? = null
        var cpuModel: String? = null
        var cpuSpeed: String? = null
        var cpuCores: String? = null
        var ramSize: String? = null
        var ramModules: String? = null
        var serialNumber: String? = null
        var totalMemory: Long = 0
        var freeMemory: Long = 0
        var busyMemory: Long = 0
        var androidDeviceId: String? = null
    }

    inner class Wifi {
        var name: String? = null
        var interfaceType: String? = null
        var model: String? = null
        var vendor: String? = null
        var ipAddress: String? = null
        var gatewayIp: String? = null
        var netmask: String? = null
        var macAddress: String? = null
        var ssid: String? = null
        var signalStrength: String? = null
        var channel: String? = null
        var security: String? = null
        var isWifiEnabled: Boolean = false
    }

    private val memoryRamSize: Long
        get() {
            val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(mInfo)
            return (mInfo.threshold shr 20)
        }

    private val processorData: Map<String, String>
        get() {
            val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(mInfo)
            val args = arrayOf("/system/bin/cat", "/proc/cpuinfo")
            val pb = ProcessBuilder(*args)
            val process: Process
            val mapData: MutableMap<String, String> = HashMap()
            try {
                process = pb.start()
                val `in` = process.inputStream
                val br = BufferedReader(InputStreamReader(`in`))
                var aLine: String
                while ((br.readLine().also { aLine = it }) != null) {
                    if ("" != aLine) {
                        try {
                            val data = aLine.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            mapData[data[0].trim { it <= ' ' }] = data[1].trim { it <= ' ' }
                        } catch (e: Exception) {
                            PreyLogger.e(String.format("Error:%s", e.message), e)
                        }
                    }
                }
                if (br != null) {
                    br.close()
                }
            } catch (e: IOException) {
                PreyLogger.e("Error:" + e.message, e)
            }
            return mapData
        }

    private val cpuCores: Int
        get() {
            val runtime = Runtime.getRuntime()
            return runtime.availableProcessors()
        }

    val iPAddress: String
        get() {
            var ip = ""
            try {
                ip = PreyWebServices.getInstance().getIPAddress(ctx)
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
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
        PreyLogger.d(String.format("isAirplaneModeOn: %s", isAirplaneModeOn))
        // Return the result
        return isAirplaneModeOn
    }

    init {
        init()
    }


    val dataState: Int
        get() {
            val tManager = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var dataState = -1
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ctx.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        dataState = tManager.dataState
                    }
                } else {
                    dataState = tManager.dataState
                }
            } catch (e: Exception) {
                PreyLogger.e("Error getDataState:" + e.message, e)
            }
            return dataState
        }


    @SuppressLint("MissingPermission")
    fun getNetworkClass(ctx: Context): String? {
        try {
            val mTelephonyManager =
                ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
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


    val serialNumber: String?
        /**
         * Retrieves the device's serial number.
         *
         * This method attempts to retrieve the serial number from various system properties.
         * If all attempts fail, it falls back to using the Build.SERIAL property.
         *
         * @return the device's serial number, or null if it could not be retrieved
         */
        get() {
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
                PreyLogger.e(String.format("Error getSerialNumber:%s", e.message), e)
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
        private var _instance: PreyPhone? = null
        fun getInstance(ctx: Context): PreyPhone? {
            if (_instance == null) _instance = PreyPhone(ctx)
            return _instance
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