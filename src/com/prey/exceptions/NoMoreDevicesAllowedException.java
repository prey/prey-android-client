/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.exceptions;


public class NoMoreDevicesAllowedException extends PreyException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6561775783677523384L;

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public NoMoreDevicesAllowedException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public NoMoreDevicesAllowedException(String message) {
		super(message);
	}

}
