/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.kotlin

import android.content.Context
import android.view.OrientationEventListener

class OrientationManager : OrientationEventListener {
    public enum class ScreenOrientation {
        REVERSED_LANDSCAPE, LANDSCAPE, PORTRAIT, REVERSED_PORTRAIT
    }

    var screenOrientation: ScreenOrientation? = null
    private var listener: OrientationListener? = null

    constructor(context: Context?, rate: Int, listener: OrientationListener?) : super(
        context,
        rate
    ) {
        setListener(listener)
    }

    constructor(context: Context?, rate: Int) : super(context, rate)

    constructor(context: Context?) : super(context)

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

    fun setListener(listener: OrientationListener?) {
        this.listener = listener
    }

    interface OrientationListener {
        fun onOrientationChange(screenOrientation: ScreenOrientation?)
    }
}