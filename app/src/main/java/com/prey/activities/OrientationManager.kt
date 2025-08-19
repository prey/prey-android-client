/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.content.Context
import android.view.OrientationEventListener

/**
 * Manages device orientation changes and notifies listeners about the current orientation.
 */
class OrientationManager : OrientationEventListener {
    public enum class ScreenOrientation {
        REVERSED_LANDSCAPE, LANDSCAPE, PORTRAIT, REVERSED_PORTRAIT
    }

    var screenOrientation: ScreenOrientation? = null
    var listener: OrientationListener? = null

    /**
     * Constructs an OrientationManager with the given context, rate, and listener.
     *
     * @param context The context to use.
     * @param rate The rate at which to receive orientation changes.
     * @param listener The listener to notify about orientation changes.
     */
    constructor(context: Context?, rate: Int, listener: OrientationListener?) : super(
        context,
        rate
    ) {
        setListener(listener)
    }

    /**
     * Constructs an OrientationManager with the given context and rate.
     *
     * @param context The context to use.
     * @param rate The rate at which to receive orientation changes.
     */
    constructor(context: Context?, rate: Int) : super(context, rate)

    /**
     * Constructs an OrientationManager with the given context.
     *
     * @param context The context to use.
     */
    constructor(context: Context?) : super(context)

    /**
     * Called when the device orientation changes.
     *
     * @param orientation The new orientation of the device.
     */
    override fun onOrientationChanged(orientation: Int) {
        if (orientation == -1) {
            return
        }
        val newOrientation = if (orientation >= 60 && orientation <= 140) {
            ScreenOrientation.REVERSED_LANDSCAPE
        } else if (orientation >= 140 && orientation <= 220) {
            ScreenOrientation.REVERSED_PORTRAIT
        } else if (orientation >= 220 && orientation <= 300) {
            ScreenOrientation.LANDSCAPE
        } else {
            ScreenOrientation.PORTRAIT
        }
        if (newOrientation != screenOrientation) {
            screenOrientation = newOrientation
            if (listener != null) {
                listener!!.onOrientationChange(screenOrientation)
            }
        }
    }

    /**
     * Sets the listener to receive orientation change notifications.
     *
     * @param listener The listener to set.
     */
    fun setListener(listener: OrientationListener?) {
        this.listener = listener
    }

    /**
     * Interface for listeners to receive orientation change notifications.
     */
    interface OrientationListener {
        fun onOrientationChange(screenOrientation: ScreenOrientation?)
    }

}