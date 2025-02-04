/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net.http

import com.prey.PreyLogger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Random

class SimpleMultipartEntity {
    private var boundary: String? = null
    var out: ByteArrayOutputStream = ByteArrayOutputStream()
    var isSetLast: Boolean = false
    var isSetFirst: Boolean = false

    init {
        val buf = StringBuffer()
        val rand = Random()
        for (i in 0..29) {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.size)])
        }
        this.boundary = buf.toString()
    }

    fun writeFirstBoundaryIfNeeds() {
        if (!isSetFirst) {
            try {
                out.write(("--$boundary\r\n").toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        isSetFirst = true
    }

    fun writeLastBoundaryIfNeeds() {
        if (isSetLast) {
            return
        }
        try {
            out.write(("\r\n--$boundary--\r\n").toByteArray())
        } catch (e: IOException) {
            PreyLogger.e("Error:" + e.message, e)
        }
        isSetLast = true
    }

    fun addPart(key: String?, value: String?) {
        writeFirstBoundaryIfNeeds()
        try {
            if (key != null && value != null) {
                out.write(("Content-Disposition: form-data; name=\"$key\"\r\n\r\n").toByteArray())
                out.write(value.toByteArray())
                out.write(("\r\n--$boundary\r\n").toByteArray())
            }
        } catch (e: IOException) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    fun addPart(key: String, fileName: String, fin: InputStream?, isLast: Boolean) {
        addPart(key, fileName, fin, "application/octet-stream", isLast)
    }

    fun addPart(
        key: String,
        fileName: String,
        fin: InputStream?,
        type: String,
        isLast: Boolean
    ): ByteArrayOutputStream? {
        var type = type
        writeFirstBoundaryIfNeeds()
        val outputStream = ByteArrayOutputStream()
        try {
            type = "Content-Type: $type\r\n"
            out.write(("Content-Disposition: form-data; name=\"$key\"; filename=\"$fileName\"\r\n").toByteArray())
            out.write(type.toByteArray())
            out.write("Content-Transfer-Encoding: binary\r\n\r\n".toByteArray())

            val tmp = ByteArray(4096)
            var l = 0
            while ((fin!!.read(tmp).also { l = it }) != -1) {
                out.write(tmp, 0, l)
                outputStream.write(tmp, 0, l)
            }
            if (!isLast) out.write(("\r\n--$boundary\r\n").toByteArray())
            out.flush()
            outputStream.flush()
        } catch (e: IOException) {
            PreyLogger.e("Error:" + e.message, e)
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    PreyLogger.e("Error:" + e.message, e)
                }
            }
            if (fin != null) {
                try {
                    fin.close()
                } catch (e: IOException) {
                    PreyLogger.e("Error:" + e.message, e)
                }
            }
        }
        return outputStream
    }

    fun addPart(key: String, value: File, isLast: Boolean) {
        try {
            addPart(key, value.name, FileInputStream(value), isLast)
        } catch (e: FileNotFoundException) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    val contentLength: Long
        get() {
            writeLastBoundaryIfNeeds()
            return out.toByteArray().size.toLong()
        }

    val contentType: String
        get() = "multipart/form-data; boundary=$boundary"

    val isChunked: Boolean
        get() = false

    val isRepeatable: Boolean
        get() = false

    val isStreaming: Boolean
        get() = false

    @Throws(IOException::class)
    fun writeTo(outstream: OutputStream) {
        outstream.write(out.toByteArray())
    }

    @Throws(IOException::class, UnsupportedOperationException::class)
    fun consumeContent() {
        if (isStreaming) {
            throw UnsupportedOperationException(
                "Streaming entity does not implement #consumeContent()"
            )
        }
    }

    @get:Throws(IOException::class, UnsupportedOperationException::class)
    val content: InputStream
        get() = ByteArrayInputStream(out.toByteArray())

    companion object {
        private val MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    }
}