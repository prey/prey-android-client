package com.prey.net.http;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;


public class PreyHttpClientImpl implements PreyHttpClient {

	private DefaultHttpClient httpClient;
	
	
	public PreyHttpClientImpl(DefaultHttpClient httpClient){
		this.httpClient=httpClient;
		
		
	}
	public HttpResponse execute(HttpUriRequest arg0) throws IOException, ClientProtocolException {
		return httpClient.execute(arg0);
	}

	public HttpResponse execute(HttpUriRequest arg0, HttpContext arg1) throws IOException, ClientProtocolException {
		return httpClient.execute(arg0,arg1);
	}

	public HttpResponse execute(HttpHost arg0, HttpRequest arg1) throws IOException, ClientProtocolException {
		return httpClient.execute(arg0,arg1);
	}

	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1) throws IOException, ClientProtocolException {
		return httpClient.execute(arg0,arg1);
	}

	public HttpResponse execute(HttpHost arg0, HttpRequest arg1, HttpContext arg2) throws IOException, ClientProtocolException {
		return httpClient.execute(arg0,arg1,arg2);
	}

	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1, HttpContext arg2) throws IOException, ClientProtocolException {
		return httpClient.execute(arg0,arg1,arg2);
	}

	public <T> T execute(HttpHost arg0, HttpRequest arg1, ResponseHandler<? extends T> arg2) throws IOException, ClientProtocolException {
		return httpClient.execute(arg0,arg1,arg2);
	}

	public <T> T execute(HttpHost arg0, HttpRequest arg1, ResponseHandler<? extends T> arg2, HttpContext arg3) throws IOException, ClientProtocolException {
		return httpClient.execute(arg0,arg1,arg2);
	}

	public ClientConnectionManager getConnectionManager() {
		return httpClient.getConnectionManager();
	}

	public HttpParams getParams() {
		return httpClient.getParams();
	}

	public void setParams(HttpParams params) {
		httpClient.setParams(params);
		
	}

	public void setRedirectHandler(RedirectHandler handler) {
		httpClient.setRedirectHandler(handler);
		
	}

 

}
