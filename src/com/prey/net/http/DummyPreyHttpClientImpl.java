package com.prey.net.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.prey.PreyLogger;
import com.prey.net.http.factory.HttpResponseFactory;

public class DummyPreyHttpClientImpl implements PreyHttpClient {

	private HttpParams params;

 	private Map<String, String> mapParams(HttpUriRequest request) {
		 
		return mapParams((HttpRequest)request);
	}

	private Map<String, String> mapParams(HttpRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		if (request != null) {
			if (request instanceof HttpPost) {
				Header[] headers = request.getAllHeaders();
				for (int i = 0; headers != null && i < headers.length; i++) {
					Header header = headers[i];
					String key = header.getName();
					String value = header.getValue();
					map.put(key, value);
					PreyLogger.i("key[" + key + "] value[" + value + "]");
				}
			}
		}
		return map;
	}

	public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
		return HttpResponseFactory.getInstance().execute(request, mapParams(request));
	}

	public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
		return HttpResponseFactory.getInstance().execute(request, mapParams(request));
	}

	public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
		return HttpResponseFactory.getInstance().execute(request, mapParams(request));
	}

	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1) throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
		return HttpResponseFactory.getInstance().execute(request, mapParams(request));
	}

	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1, HttpContext arg2) throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T execute(HttpHost arg0, HttpRequest arg1, ResponseHandler<? extends T> arg2) throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T execute(HttpHost arg0, HttpRequest arg1, ResponseHandler<? extends T> arg2, HttpContext arg3) throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	public ClientConnectionManager getConnectionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpParams getParams() {
		return params;
	}

	public void setParams(HttpParams params) {
		this.params = params;
	}

	public void setRedirectHandler(RedirectHandler handler) {
		// TODO Auto-generated method stub

	}

}
