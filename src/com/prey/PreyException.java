package com.prey;

public class PreyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public PreyException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public PreyException(String message) {
		super(message);
	}

}
