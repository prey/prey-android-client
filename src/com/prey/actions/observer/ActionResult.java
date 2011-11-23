/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.observer;

import com.prey.actions.HttpDataService;

public class ActionResult {

	private String result;
	private HttpDataService dataToSend;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public HttpDataService getDataToSend() {
		return dataToSend;
	}

	public void setDataToSend(HttpDataService dataToSend) {
		this.dataToSend = dataToSend;
	}

}
