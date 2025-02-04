/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.util

import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import com.prey.PreyLogger
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL


class HttpUtil {

    companion object {
        fun getContents(url: String?): String {
            var contents = ""
            var `in`: InputStream? = null
            try {
                var policy: ThreadPolicy? = null
                policy = ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val conn = URL(url).openConnection()
                `in` = conn.getInputStream()
                contents = convertStreamToString(`in`)
            } catch (e: Exception) {
                PreyLogger.e("getContents error:" + e.message, e)
            } finally {
                if (`in` != null) {
                    try {
                        `in`.close()
                    } catch (e: Exception) {
                    }
                }
            }
            return contents
        }

        private fun convertStreamToString(`is`: InputStream?): String {
            var inReader: InputStreamReader? = null
            var reader: BufferedReader? = null
            var sb: StringBuilder? = null
            try {
                inReader = InputStreamReader(`is`, "UTF-8")
                reader = BufferedReader(inReader)
                sb = StringBuilder()
                var line: String? = null
                while ((reader.readLine().also { line = it }) != null) {
                    sb.append(line + "\n")
                }
            } catch (e: Exception) {
            } finally {
                try {
                    reader?.close()
                } catch (e: IOException) {
                }
                try {
                    inReader?.close()
                } catch (e: IOException) {
                }
                try {
                    `is`?.close()
                } catch (e: IOException) {
                }
            }
            return sb?.toString() ?: ""
        }
    }

}