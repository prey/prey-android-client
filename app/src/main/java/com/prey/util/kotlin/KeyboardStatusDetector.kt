/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.util.kotlin

import android.R
import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.prey.util.KeyboardVisibilityListener

/**
 * Detects Keyboard Status changes and fires events only once for each change
 */
class KeyboardStatusDetector {
    var visibilityListener: KeyboardVisibilityListener? = null
    var keyboardVisible: Boolean = false

    fun registerActivity(a: Activity) {
        registerView(a.window.decorView.findViewById(R.id.content))
    }

    fun registerView(v: View): KeyboardStatusDetector {
        v.getViewTreeObserver().addOnGlobalLayoutListener(OnGlobalLayoutListener {
            val r = Rect()
            v.getWindowVisibleDisplayFrame(r)
            val heightDiff: Int = v.getRootView().getHeight() - (r.bottom - r.top)
            if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                /** Check this variable to debounce layout events  */
                /** Check this variable to debounce layout events  */
                /** Check this variable to debounce layout events  */
                /** Check this variable to debounce layout events  */
                if (!keyboardVisible) {
                    keyboardVisible = true
                    if (visibilityListener != null) visibilityListener!!.onVisibilityChanged(true)
                }
            } else {
                if (keyboardVisible) {
                    keyboardVisible = false
                    if (visibilityListener != null) visibilityListener!!.onVisibilityChanged(false)
                }
            }
        })
        return this
    }

    fun setVisibilityListener(listener: KeyboardVisibilityListener?): KeyboardStatusDetector {
        visibilityListener = listener
        return this
    }
}