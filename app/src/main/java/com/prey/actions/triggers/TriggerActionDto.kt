/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

/**
 * Data Transfer Object (DTO) for Trigger Actions.
 * This class represents a trigger action with a delay and an action.
 */
class TriggerActionDto {

    /**
     * The delay in seconds before the action is executed.
     */
    private var delay: Int = 0
    /**
     * The action to be executed. This can be a JSON string representing the action.
     */
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