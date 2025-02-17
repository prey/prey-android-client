/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

/**
 * Data transfer object for trigger events.
 *
 * This class represents a trigger event with its type and additional information.
 */
class TriggerEventDto {

    /**
     * The type of the trigger event.
     */
    private var type: String? = null
    /**
     * Additional information about the trigger event.
     */
    private var info: String? = null

    fun getType(): String {
        return type!!
    }

    fun setType(type: String?) {
        this.type = type
    }

    fun getInfo(): String {
        return info!!
    }

    fun setInfo(info: String?) {
        this.info = info
    }
}
