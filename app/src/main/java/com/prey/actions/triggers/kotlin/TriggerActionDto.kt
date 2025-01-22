/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers.kotlin

class TriggerActionDto {
    private var delay: Int = 0
    private var action: String? = null
    fun getDelay(): Int {
        return delay
    }

    fun setDelay(delay: Int) {
        this.delay = delay
    }

    fun getAction(): String {
        return action!!
    }

    fun setAction(action: String?) {
        this.action = action
    }
}