/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.services.kotlin

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.prey.beta.actions.kotlin.PreyBetaActionsRunner
import com.prey.kotlin.PreyLogger

/**
 * This class wraps Prey execution as a services, allowing the OS to kill it and
 * starting it again in case of low resources. This way we ensure Prey will be
 * running until explicity stop it.
 *
 * @author Carlos Yaconi H.
 */
class PreyBetaRunnerService : Service() {
    private val mBinder: IBinder = LocalBinder()

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        val service: PreyBetaRunnerService
            get() = this@PreyBetaRunnerService
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        var cmd: String? = null
        try {
            if (intent?.extras != null && intent.extras!!.containsKey("cmd")) {
                cmd = intent.extras!!.getString("cmd")
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        PreyLogger.d("PreyBetaActionsRunner has been started...:$cmd")
        val exec = PreyBetaActionsRunner( applicationContext, cmd)
        running = true
        exec.run()
    }

    override fun onDestroy() {

        running = false
    }

    override fun onBind(arg0: Intent): IBinder? {
        return mBinder
    }

    companion object {
        var running: Boolean = false
    }
}