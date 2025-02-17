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

/**
 * A utility class for making HTTP requests and converting input streams to strings.
 */
class HttpUtil {

    /**
     * Retrieves the contents of a URL as a string.
     *
     * @param url the URL to retrieve contents from
     * @return the contents of the URL as a string
     */
    fun getContents(url: String): String {
        var contents = ""
        var inputStream: InputStream? = null
        try {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val connection = URL(url).openConnection()
            inputStream = connection.getInputStream()
            contents = convertStreamToString(inputStream)
        } catch (e: Exception) {
            PreyLogger.e("getContents error:" + e.message, e)
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: Exception) {
                }
            }
        }
        return contents
    }

    /**
     * Converts an input stream to a string.
     *
     * @param inputStream the input stream to convert
     * @return the converted string
     */
    private fun convertStreamToString(inputStream: InputStream?): String {
        var inReader: InputStreamReader? = null
        var reader: BufferedReader? = null
        var sb: StringBuilder? = null
        try {
            inReader = InputStreamReader(inputStream, "UTF-8")
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
                inputStream?.close()
            } catch (e: IOException) {
            }
        }
        return sb?.toString() ?: ""
    }

}