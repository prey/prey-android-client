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

/**
 * A simple multipart entity class for handling HTTP multipart requests.
 */
class SimpleMultipartEntity {
    private var boundary: String? = null
    var out: ByteArrayOutputStream = ByteArrayOutputStream()
    var isSetLast: Boolean = false
    var isSetFirst: Boolean = false

    /**
     * Initializes the multipart entity with a random boundary string.
     */
    init {
        val buf = StringBuffer()
        val rand = Random()
        for (i in 0..29) {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.size)])
        }
        this.boundary = buf.toString()
    }

    /**
     * Writes the first boundary to the output stream if it hasn't been written yet.
     */
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

    /**
     * Writes the last boundary to the output stream if it hasn't been written yet.
     */
    fun writeLastBoundaryIfNeeds() {
        if (isSetLast) {
            return
        }
        try {
            out.write(("\r\n--$boundary--\r\n").toByteArray())
        } catch (e: IOException) {
            PreyLogger.e("Error:${e.message}", e)
        }
        isSetLast = true
    }

    /**
     * Adds a part to the multipart request with the given key and value.
     *
     * @param key The key for the part
     * @param value The value for the part
     */
    fun addPart(key: String?, value: String?) {
        writeFirstBoundaryIfNeeds()
        try {
            if (key != null && value != null) {
                out.write(("Content-Disposition: form-data; name=\"$key\"\r\n\r\n").toByteArray())
                out.write(value.toByteArray())
                out.write(("\r\n--$boundary\r\n").toByteArray())
            }
        } catch (e: IOException) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }

    /**
     * Adds a part to the multipart request with the given key, file name, and input stream.
     *
     * @param key The key for the part
     * @param fileName The file name for the part
     * @param fin The input stream for the part
     * @param isLast Whether this is the last part
     * @return The output stream for the part
     */
    fun addPart(key: String, fileName: String, fin: InputStream?, isLast: Boolean) {
        addPart(key, fileName, fin, "application/octet-stream", isLast)
    }

    /**
     * Adds a part to the multipart request with the given key, file name, input stream, and content type.
     *
     * @param key The key for the part
     * @param fileName The file name for the part
     * @param fin The input stream for the part
     * @param type The content type for the part
     * @param isLast Whether this is the last part
     * @return The output stream for the part
     */
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
            PreyLogger.e("Error:${e.message}", e)
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    PreyLogger.e("Error:${e.message}", e)
                }
            }
            if (fin != null) {
                try {
                    fin.close()
                } catch (e: IOException) {
                    PreyLogger.e("Error:${e.message}", e)
                }
            }
        }
        return outputStream
    }

    /**
     * Adds a part to the multipart request with the given key and file.
     *
     * @param key The key for the part
     * @param value The file for the part
     * @param isLast Whether this is the last part
     */
    fun addPart(key: String, value: File, isLast: Boolean) {
        try {
            addPart(key, value.name, FileInputStream(value), isLast)
        } catch (e: FileNotFoundException) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }

    /**
     * Returns the length of the content in bytes.
     *
     * @return The length of the content
     */
    fun contentLength(): Long {
        writeLastBoundaryIfNeeds()
        return out.toByteArray().size.toLong()
    }

    /**
     * Returns the content type of the multipart request.
     *
     * @return The content type
     */
    fun contentType(): String = "multipart/form-data; boundary=$boundary"

    fun isChunked(): Boolean = false

    fun isRepeatable(): Boolean = false

    fun isStreaming(): Boolean = false

    /**
     * Writes the content to the given output stream.
     *
     * @param outstream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    @Throws(IOException::class)
    fun writeTo(outstream: OutputStream) {
        outstream.write(out.toByteArray())
    }

    /**
     * Consumes the content of the entity.
     *
     * @throws IOException If an I/O error occurs
     * @throws UnsupportedOperationException If the entity is streaming
     */
    @Throws(IOException::class, UnsupportedOperationException::class)
    fun consumeContent() {
        if (isStreaming()) {
            throw UnsupportedOperationException(
                "Streaming entity does not implement #consumeContent()"
            )
        }
    }

    /**
     * Returns the content of the entity as an input stream.
     *
     * @return The content of the entity as an input stream
     * @throws IOException If an I/O error occurs
     * @throws UnsupportedOperationException If the entity is streaming
     */
    @Throws(IOException::class, UnsupportedOperationException::class)
    fun getContent(): InputStream = ByteArrayInputStream(out.toByteArray())

    companion object {
        private val MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    }
}