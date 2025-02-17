/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import java.util.StringTokenizer

/**
 * Utility class for Prey-related functionality.
 */
object PreyUtils {

    fun getDeviceType(act: Activity): String {
        return getDeviceType(act.applicationContext)
    }

    const val LAPTOP: String = "Laptop"

    fun getDeviceType(context: Context): String {
        return if (isChromebook(context)) {
            LAPTOP
        } else {
            if (isTablet(context)) {
                "Tablet"
            } else {
                "Phone"
            }
        }
    }

    @Throws(Exception::class)
    fun getNameDevice(context: Context): String {
        var newName = ""
        var name: String? = null
        val model = Build.MODEL
        var vendor = "Google"
        try {
            vendor = Build.MANUFACTURER
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        try {
            name = Settings.Secure.getString(context.contentResolver, "bluetooth_name")
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        newName = if (name != null && "" != name) {
            name
        } else {
            "$vendor $model"
        }
        return newName
    }

    fun isChromebook(context: Context): Boolean {
        return PreyConfig.getInstance(context).isChromebook()
    }

    fun isTablet(context: Context): Boolean {
        try {
            val dm = context.resources.displayMetrics
            val screenWidth = dm.widthPixels / dm.xdpi
            val screenHeight = dm.heightPixels / dm.ydpi
            //TODO: OSO cambiar
            val size = 9//sqrt(screenWidth.pow(2.0) + screenHeight.pow(2.0))
            return size >= 7.0
        } catch (t: Throwable) {
            return false
        }
    }

    fun randomAlphaNumeric(length: Int): String {
        val buffer = StringBuffer()
        val characters = "abcdefghijklmnopqrstuvwxyz0123456789"
        val charactersLength = characters.length
        for (i in 0 until length) {
            val index = Math.random() * charactersLength
            buffer.append(characters[index.toInt()])
        }
        return buffer.toString()
    }

    fun getBuildVersionRelease(): String {
        var version = ""
        try {
            val release = Build.VERSION.RELEASE
            val st = StringTokenizer(release, ".")
            var first = true
            while (st.hasMoreElements()) {
                val number = st.nextToken()
                //if (number != null)
                //number = number.substring(0, 1);
                version = if ((first)) number else "$version.$number"
                first = false
            }
        } catch (e: java.lang.Exception) {
        }
        return version
    }

    fun getLanguage(): String {
        return if ("es" == Locale.getDefault().language) "es" else "en"
    }

    fun toast(context: Context?, out: String?) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            if (out != null && "" != out) {
                Toast.makeText(context, out, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private const val EOF = -1
    private const val DEFAULT_BUFFER_SIZE = 1024 * 4

    /**
     * Method copy files
     * @param input
     * @param output
     * @return quantity copied
     * @throws IOException
     */
    @Throws(IOException::class)
    fun copyFile(input: InputStream, output: OutputStream): Long {
        var count: Long = 0
        var n: Int
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (EOF != (input.read(buffer).also { n = it })) {
            output.write(buffer, 0, n)
            count += n.toLong()
        }
        return count
    }

    fun isGooglePlayServicesAvailable(context: Context?): Boolean {
        var isGooglePlayServicesAvailable: Boolean
        try {
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context!!)
            isGooglePlayServicesAvailable = (resultCode == ConnectionResult.SUCCESS)
        } catch (e: Exception) {
            isGooglePlayServicesAvailable = false
        }
        PreyLogger.d(

            "isGooglePlayServicesAvailable:${isGooglePlayServicesAvailable}"
        )
        return isGooglePlayServicesAvailable
    }

}