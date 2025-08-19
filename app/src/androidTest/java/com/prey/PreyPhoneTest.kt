package com.prey

import android.app.ActivityManager
import android.content.Context
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider

import com.prey.net.TestWebServices

import org.junit.Before

class PreyPhoneTest {

    private lateinit var preyPhone: PreyPhone
    private lateinit var context: Context
    private lateinit var wifiManager: WifiManager
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var activityManager: ActivityManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        preyPhone = PreyPhone(context)
        wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

}