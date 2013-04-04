package com.prey.net.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.prey.PreyLogger;
import com.prey.net.http.factory.HttpResponseFactory;

public class DummyPreyHttpClientImpl implements PreyHttpClient {

	private HttpParams params;
	
	
	private HttpResponse getResponse200(){
		
				
		
		int statusCode=200;
		String reasonPhrase="OK";
		ProtocolVersion protocolVersion=new ProtocolVersion("HTTP", 1, 1);
		StatusLine statusLine=new BasicStatusLine(protocolVersion, statusCode, reasonPhrase);
		HttpResponse response=new BasicHttpResponse(statusLine);
		
		BasicHttpEntity httpEntity =new BasicHttpEntity();
		String htmlTxt="[ {\"command\": \"data\",\"target\": \"location\",\"options\": {}}]";
		
		InputStream stream = new ByteArrayInputStream(htmlTxt.getBytes());
		
		httpEntity.setContent(stream);
		
		response.setEntity(httpEntity);
		return response;
	}
	
	private void printParams(HttpUriRequest request) {
		if (request != null) {
			if (request instanceof HttpPost) {
				Header[] headers = request.getAllHeaders();
				for (int i = 0; headers != null && i < headers.length; i++) {
					Header header = headers[i];
					String key = header.getName();
					String value = header.getValue();
					PreyLogger.i("key[" + key + "] value[" + value + "]");
				}
			}
		}
	}
	
	public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
	//	PreyLogger.i("url:"+request.getURI().toURL());
	//	printParams(request);
	//	return getResponse200();
		
		return HttpResponseFactory.getInstance().execute(request);
	}

	public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return getResponse200();
	}

	public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return getResponse200();
	}

	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1) throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return getResponse200();
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
		this.params=params;
	}

	public void setRedirectHandler(RedirectHandler handler) {
		// TODO Auto-generated method stub

	}

 

}
