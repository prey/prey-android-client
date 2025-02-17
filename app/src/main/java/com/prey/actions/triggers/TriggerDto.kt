/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

/**
 * Data Transfer Object (DTO) for Triggers.
 * This class represents a trigger with an ID, name, events, actions, and lists of events and actions.
 */
class TriggerDto {

    /**
     * Unique identifier for the trigger.
     */
    private var id: Int = 0
    /**
     * Name of the trigger.
     */
    private var name: String? = null
    /**
     * String representation of the trigger events.
     */
    private var events: String? = null
    /**
     * String representation of the trigger actions.
     */
    private var actions: String? = null
    /**
     * List of TriggerEventDto objects representing the trigger events.
     */
    private var listEvents: List<TriggerEventDto>? = null
    /**
     * List of TriggerActionDto objects representing the trigger actions.
     */
    private var listActions: List<TriggerActionDto>? = null

    fun getId(): Int {
        return id
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getName(): String {
        return name!!
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getEvents(): String {
        return events!!
    }

    fun setEvents(events: String?) {
        this.events = events
    }

    fun getActions(): String {
        return actions!!
    }

    fun setActions(actions: String?) {
        this.actions = actions
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("id:").append(id)
        sb.append(" name:").append(name)
        sb.append(" events:").append(events)
        sb.append(" actions:").append(actions).append("\n")
        return sb.toString()
    }

    fun setId(id: String) {
        this.id = id.toInt()
    }
}
