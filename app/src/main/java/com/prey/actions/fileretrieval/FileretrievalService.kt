/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval

import android.app.IntentService
import android.content.Intent
import com.prey.PreyConfig
import com.prey.PreyLogger

/**
 * A service responsible for handling file retrieval operations.
 */
class FileretrievalService : IntentService {
    constructor() : super(PreyConfig.TAG)

    constructor(name: String?) : super(name)

    /**
     * Handles the intent sent to this service.
     *
     * This method is called on the worker thread.
     *
     * @param intent The intent to handle.
     */
    override fun onHandleIntent(intent: Intent?) {
        PreyLogger.d("***************onHandleIntent")
        FileretrievalController.getInstance().run(applicationContext)
        stopSelf()
    }
}