/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

class TriggerEventDto {
    private var type: String? = null
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
