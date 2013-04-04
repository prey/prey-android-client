package com.prey.net.http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.params.HttpParams;

public interface PreyHttpClient extends HttpClient {
	
	
	public void setParams(HttpParams params);

	public void setRedirectHandler(RedirectHandler handler);
}
