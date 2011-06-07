package com.prey;

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
		// TODO Auto-generated constructor stub
	}

	public NoMoreDevicesAllowedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
