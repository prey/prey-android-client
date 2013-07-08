/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

import com.prey.PreyLogger;

public class PreyHttpResponse {

	private StatusLine statusLine;
	private String responseAsString;
	private HttpResponse response;

	// public PreyHttpResponse(HttpMethod method){
	// this.statusLine = method.getStatusLine();
	//
	// try {
	// this.responseAsString =
	// convertStreamToString(method.getResponseBodyAsStream());
	// } catch (IOException e) {
	// Log.d(
	// "Can't receive body stream from http connection, setting response string as ''");
	// this.responseAsString = "";
	// }
	// }

	public PreyHttpResponse(HttpResponse response) {
		this.response=response;
		this.statusLine = response.getStatusLine();

		try {
			this.responseAsString = convertStreamToString(response.getEntity().getContent());
		} catch (IOException e) {
			PreyLogger.d("Can't receive body stream from http connection, setting response string as ''");
			this.responseAsString = "";
		}
	}

	public PreyHttpResponse(InputStream responseStream, StatusLine statusLine) {
		this.statusLine = statusLine;
		this.responseAsString = convertStreamToString(responseStream);
	}

	private String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public StatusLine getStatusLine() {
		return statusLine;
	}

	public String getResponseAsString() {
		return responseAsString;
	}

	@Override
	public String toString() {

		return statusLine.toString() + " " + responseAsString;
	}

	public HttpResponse getResponse() {
		return response;
	}

	public void setResponse(HttpResponse response) {
		this.response = response;
	}

}
