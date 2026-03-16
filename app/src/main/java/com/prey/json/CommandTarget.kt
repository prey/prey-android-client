/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json

import android.content.Context
import org.json.JSONObject

/**
 * Defines a contract for objects that can receive and execute commands.
 * Implementations of this interface are responsible for handling specific commands
 * identified by a string and processing any associated options provided in a JSONObject.
 */
interface CommandTarget {
    fun execute(context: Context, command: String, options: JSONObject)
}