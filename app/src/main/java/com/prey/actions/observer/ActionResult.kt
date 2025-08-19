/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.observer

import com.prey.actions.HttpDataService

/**
 * Represents the result of an action, including the outcome and any data to be sent.
 */
class ActionResult {
    /**
     * The result of the action, represented as a string.
     */
    var result: String? = null

    /**
     * The data to be sent as a result of the action, represented as an HttpDataService object.
     */

    var dataToSend: HttpDataService? = null

}