/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers;

import java.util.List;

public class TriggerDto {
    public int id;
    public String name;

    public String events;
    public String actions;
    public List<TriggerEventDto> listEvents;
    public List<TriggerActionDto> listActions;

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("id:").append(id);
        sb.append(" name:").append(name);
        sb.append(" events:").append(events);
        sb.append(" actions:").append(actions).append("\n");
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = Integer.parseInt(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEvents() {
        return events;
    }

    public void setEvents(String events) {
        this.events = events;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }
}
