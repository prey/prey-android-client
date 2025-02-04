/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.actions

import android.content.Context

class PreyBetaActionsRunnner {

    protected var running: Boolean = false
    private var myActionsRunnerThread: Thread? = null
    private var cmd: String? = null

    fun PreyBetaActionsRunnner(cmd: String) {
        this.cmd = cmd
    }

    fun run(ctx: Context) {
        this.myActionsRunnerThread = Thread(PreyBetaActionsRunner(ctx, cmd))
        myActionsRunnerThread!!.start()
    }

}