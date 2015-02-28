/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import org.json.JSONException;
import org.json.JSONObject;

import com.prey.exceptions.PreyException;

public class PushMessage {
	
	private String event;
	private String type;
	private String body;
	
	public PushMessage(String jsonMessage) throws PreyException{
		try {
			JSONObject jsonObj = new JSONObject(jsonMessage);
			JSONObject data = jsonObj.getJSONObject("data");
			this.event = jsonObj.getString("event");
			this.type = data.getString("type");
			this.body = data.getString("body");
		} catch (JSONException e) {
			throw new PreyException("Couldn't parse pushed json message");
		}
	}
	
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
}
