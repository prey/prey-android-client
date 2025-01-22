/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.kotlin

import android.content.Context
import android.content.res.Resources.NotFoundException
import com.prey.R
import java.io.IOException
import java.util.Properties

class PreyBatch private constructor(ctx: Context) {
    private val properties = Properties()

    init {
        try {
            PreyLogger.d("Loading config batch properties from file...")
            val file = ctx.resources.openRawResource(R.raw.batch)
            properties.load(file)
            file.close()
            PreyLogger.d("Batch Config: $properties")
        } catch (e: NotFoundException) {
            PreyLogger.e("Batch Config file wasn't found", e)
        } catch (e: IOException) {
            PreyLogger.e("Couldn't read config file", e)
        }
    }

    val apiKeyBatch: String
        /**
         * Method get api key
         * @return
         */
        get() = properties!!.getProperty("api-key-batch")

    val emailBatch: String
        /**
         * Method get email
         * @return
         */
        get() = properties!!.getProperty("email-batch")

    val isAskForNameBatch: Boolean
        /**
         * Method if it asks for the name
         * @return
         */
        get() = properties!!.getProperty("ask-for-name-batch").toBoolean()

    val token: String
        /**
         * Method get token
         * @return
         */
        get() = properties!!.getProperty("token")

    val isThereBatchInstallationKey: Boolean
        /**
         * Method returns if it has apikey batch
         * @return
         */
        get() {
            val apiKeyBatch = _instance!!.apiKeyBatch
            return (apiKeyBatch != null && "" != apiKeyBatch)
        }

    companion object {
        private var _instance: PreyBatch? = null
        fun getInstance(ctx: Context): PreyBatch? {
            if (_instance == null) _instance = PreyBatch(ctx)
            return _instance
        }
    }
}
