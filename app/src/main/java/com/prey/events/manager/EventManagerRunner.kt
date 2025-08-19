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

/**
 * EventManagerRunner is a Runnable class responsible for executing events.
 *
 * @param context The application context.
 * @param event The event to be executed.
 */
class EventManagerRunner(var context: Context, var event: Event) : Runnable {

    /**
     * Runs the event execution process.
     */
    override fun run() {
        PreyLogger.d("EVENT CheckInReceiver IN:${event.name}")
        EventManager(context).execute(event)
        PreyLogger.d("EVENT CheckInReceiver OUT:${event.name}")
    }

}
