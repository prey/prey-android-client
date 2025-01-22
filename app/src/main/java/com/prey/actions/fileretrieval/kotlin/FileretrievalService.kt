/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval.kotlin

import android.app.IntentService
import android.content.Intent
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger

class FileretrievalService : IntentService {
    constructor() : super(PreyConfig.TAG)

    constructor(name: String?) : super(name)

    override fun onHandleIntent(intent: Intent?) {
        PreyLogger.d("***************onHandleIntent")
        FileretrievalController.getInstance().run(applicationContext)
        stopSelf()
    }
}