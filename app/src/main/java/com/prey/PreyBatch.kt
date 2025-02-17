/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

import android.content.Context
import android.content.res.Resources
import java.io.IOException
import java.util.Properties

/**
 * A class representing a Prey batch configuration.
 *
 * This class loads configuration properties from a file named "batch" in the raw resources.
 * It provides methods to retrieve the API key, email, and token from the configuration.
 */
class PreyBatch private constructor(context: Context) {
    private val properties = Properties()

    /**
     * Initializes the PreyBatch instance by loading the configuration from the file.
     *
     * @param context The application context.
     */
    init {
        try {
            PreyLogger.d("Loading config batch properties from file...")
            val file = context.resources.openRawResource(R.raw.batch)
            properties.load(file)
            file.close()
            PreyLogger.d("Batch Config: $properties")
        } catch (e: Resources.NotFoundException) {
            PreyLogger.e("Batch Config file wasn't found", e)
        } catch (e: IOException) {
            PreyLogger.e("Couldn't read config file", e)
        }
    }

    /**
     * Method get api key
     * @return
     */
    fun getApiKeyBatch(): String {
        return properties.getProperty("api-key-batch")
    }

    /**
     * Method get email
     * @return
     */
    fun getEmailBatch(): String {
        return properties.getProperty("email-batch")
    }

    /**
     * Method if it asks for the name
     * @return
     */
    fun isAskForNameBatch(): Boolean {
        return properties.getProperty("ask-for-name-batch").toBoolean()
    }

    /**
     * Method get token
     * @return
     */
    fun getToken(): String {
        return properties.getProperty("token")
    }

    /**
     * Method returns if it has apikey batch
     * @return
     */
    fun isThereBatchInstallationKey(): Boolean {
        val apiKeyBatch = getApiKeyBatch()
        return (apiKeyBatch != null && "" != apiKeyBatch)
    }

    companion object {
        private var instance: PreyBatch? = null
        fun getInstance(context: Context): PreyBatch {
            if (instance == null) instance = PreyBatch(context)
            return instance!!
        }
    }
}