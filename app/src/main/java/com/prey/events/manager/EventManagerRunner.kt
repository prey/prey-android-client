/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager

import android.content.Context
import com.prey.events.Event
import com.prey.PreyLogger

class EventManagerRunner(ctx: Context, event: Event?) : Runnable {
    private var ctx: Context? = null
    private val event: Event?

    init {
        this.ctx = ctx
        this.event = event
    }

    override fun run() {
        if (event != null) {
            PreyLogger.d("EVENT CheckInReceiver IN:" + event.name)
            EventManager(ctx!!).execute(event)
            PreyLogger.d("EVENT CheckInReceiver OUT:" + event.name)
        }
    }
}
