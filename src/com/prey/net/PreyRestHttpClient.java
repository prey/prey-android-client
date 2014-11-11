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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;

import com.prey.exceptions.PreyException;
import com.prey.net.http.EntityFile;
import com.prey.net.http.SimpleMultipartEntity;

/**
 * Implements a Rest API using android http client.
 * 
 * @author Carlos Yaconi H.
 * 
 */
public class PreyRestHttpClient {

	private static PreyRestHttpClient _instance = null;
	private PreyDefaultHttpClient httpclient = null;
	private PreyDefaultHttpClient httpclientDefault = null;
	//private DefaultHttpClient httpclient = null;
	private Context ctx = null;

	private PreyRestHttpClient(Context ctx) {
		this.ctx = ctx;
		httpclient = new PreyDefaultHttpClient((DefaultHttpClient) HttpUtils.getNewHttpClient());
		httpclientDefault= new PreyDefaultHttpClient((DefaultHttpClient) HttpUtils.getNewHttpClient());

		HttpParams params = new BasicHttpParams();

		// Set the timeout in milliseconds until a connection is established.
		int timeoutConnection = 30000;
		HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);

		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 50000;
		HttpConnectionParams.setSoTimeout(params, timeoutSocket);

		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF_8");
		HttpProtocolParams.setUseExpectContinue(params, false);
		HttpParams paramsDefault=params;
		httpclientDefault.setParams(paramsDefault);
		HttpProtocolParams.setUserAgent(params, getUserAgent());
		httpclient.setParams(params);
	}

	public static PreyRestHttpClient getInstance(Context ctx) {

		_instance = new PreyRestHttpClient(ctx);
		return _instance;

	}

	private static List<NameValuePair> getHttpParamsFromMap(Map<String, String> params) {

		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		for (Iterator<Map.Entry<String, String>> it = params.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			// httpParams.setParameter(key, value);
			parameters.add(new BasicNameValuePair(key, value));
		}
		return parameters;
	}

	public PreyHttpResponse methodAsParameter(String url, String methodAsString, Map<String, String> params, PreyConfig preyConfig, String user, String pass) throws IOException {
		HttpPost method = new HttpPost(url);
		params.put("_method", methodAsString);
		method.setEntity(new UrlEncodedFormEntity(getHttpParamsFromMap(params), HTTP.UTF_8));
		// method.setQueryString(getHttpParamsFromMap(params));
		PreyLogger.d("Sending using user,pass " + methodAsString + "(using _method) - URI: " + url + " - parameters: " + params.toString());
		return sendUsingMethodUsingCredentials(method, preyConfig, user, pass);
	}

	public PreyHttpResponse methodAsParameter(String url, String methodAsString, Map<String, String> params, PreyConfig preyConfig) throws IOException {
		HttpPost method = new HttpPost(url);
		params.put("_method", methodAsString);
		method.setEntity(new UrlEncodedFormEntity(getHttpParamsFromMap(params), HTTP.UTF_8));
		// method.setQueryString(getHttpParamsFromMap(params));
		//PreyLogger.d("Sending using " + methodAsString + "(using _method) - URI: " + url + " - parameters: " + params.toString());
		return sendUsingMethod(method, preyConfig);
	}

	public PreyHttpResponse put(String url, Map<String, String> params, PreyConfig preyConfig) throws IOException {
		HttpPut method = new HttpPut(url);
		method.setHeader("Accept", "*/*");
		method.setEntity(new UrlEncodedFormEntity(getHttpParamsFromMap(params), HTTP.UTF_8));
		// method.setParams(getHttpParamsFromMap(params));
		PreyLogger.d("Sending using 'PUT' - URI: " + url + " - parameters: " + params.toString());
		HttpResponse httpResponse = httpclient.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		//PreyLogger.d("Response from server: " + response.toString());
		return response;
	}

	public PreyHttpResponse put(String url, Map<String, String> params, PreyConfig preyConfig, String user, String pass) throws IOException {
		HttpPut method = new HttpPut(url);
		method.setHeader("Accept", "*/*");
		method.addHeader("Authorization", "Basic " + getCredentials(user, pass));
		method.setEntity(new UrlEncodedFormEntity(getHttpParamsFromMap(params), HTTP.UTF_8));
		//method.setParams(getHttpParamsFromMap(params));
		//PreyLogger.d("Sending using 'PUT' (Basic Authentication) - URI: " + url + " - parameters: " + params.toString());
		HttpResponse httpResponse = httpclient.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		//PreyLogger.d("Response from server: " + response.toString());
		return response;
	}

	public PreyHttpResponse post(String url, Map<String, String> params) throws IOException {
 
		
		HttpPost method = new HttpPost(url);

		method.setHeader("Accept", "*/*");
		method.setEntity(new UrlEncodedFormEntity(getHttpParamsFromMap(params), HTTP.UTF_8));
 

		// method.setParams(getHttpParamsFromMap(params));
		PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + params.toString());
		httpclient.setRedirectHandler(new NotRedirectHandler());
		HttpResponse httpResponse = httpclient.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		//PreyLogger.d("Response from server: " + response.toString());
		return response;
	}

	public PreyHttpResponse post(String url, Map<String, String> params, List<EntityFile> entityFiles) throws IOException {
		HttpPost method = new HttpPost(url);
		method.setHeader("Accept", "*/*");
		SimpleMultipartEntity entity = new SimpleMultipartEntity();
		for (Iterator<Map.Entry<String, String>> it = params.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			entity.addPart(key, value);
		}
		try{
			for (int i=0;i< entityFiles.size();i++) {
				EntityFile entityFile =entityFiles.get(i);
				boolean isLast=((i+1)==entityFiles.size()?true:false);
//				PreyLogger.d("["+i+"]type:"+entityFile.getType()+" name:"+entityFile.getName()+ " File:" + entityFile.getFile() + " MimeType:" + entityFile.getMimeType()+" isLast:"+isLast);
				entity.addPart(entityFile.getType(), entityFile.getName(), entityFile.getFile(), entityFile.getMimeType(), isLast);
			}
		}catch(Exception e){
			
		}

		method.setEntity(entity);
		PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + params.toString());
		httpclient.setRedirectHandler(new NotRedirectHandler());
		HttpResponse httpResponse = httpclient.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		PreyLogger.d("Response from server: " + response.toString());
		return response;
	}

	public PreyHttpResponse postAutentication(String url, Map<String, String> params, PreyConfig preyConfig, List<EntityFile> entityFiles) throws IOException {
		HttpPost method = new HttpPost(url);
		method.setHeader("Accept", "*/*");
		method.addHeader("Authorization", "Basic " + getCredentials(preyConfig.getApiKey(), "X"));

		SimpleMultipartEntity entity = new SimpleMultipartEntity();
		for (Iterator<Map.Entry<String, String>> it = params.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			entity.addPart(key, value);
		}
		for (int i=0;i< entityFiles.size();i++) {
			EntityFile entityFile =entityFiles.get(i);
			boolean isLast=((i+1)==entityFiles.size()?true:false);
//			PreyLogger.d("["+i+"]type:"+entityFile.getType()+" name:"+entityFile.getName()+ " File:" + entityFile.getFile() + " MimeType:" + entityFile.getMimeType()+" isLast:"+isLast);
			entity.addPart(entityFile.getType(), entityFile.getName(), entityFile.getFile(), entityFile.getMimeType(), isLast);
		}

		method.setEntity(entity);
		PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + params.toString());
		httpclient.setRedirectHandler(new NotRedirectHandler());
		HttpResponse httpResponse = httpclient.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		//PreyLogger.d("Response from server: " + response.toString());
		return response;
	}

	public PreyHttpResponse postStatusAutentication(String url, String status, Map<String, String> params, PreyConfig preyConfig) throws IOException {
		HttpPost method = new HttpPost(url);

		method.setHeader("Accept", "*/*");
		method.setEntity(new UrlEncodedFormEntity(getHttpParamsFromMap(params), HTTP.UTF_8));
		method.addHeader("Authorization", "Basic " + getCredentials(preyConfig.getApiKey(), "X"));
		method.addHeader("X-Prey-Status", status);
		PreyLogger.i("status " + status);

		// method.setParams(getHttpParamsFromMap(params));
		PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + params.toString());
		httpclient.setRedirectHandler(new NotRedirectHandler());
		HttpResponse httpResponse = httpclient.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		//PreyLogger.d("Response from server: " + response.toString());
		return response;
	}

	public PreyHttpResponse postAutentication(String url, Map<String, String> params, PreyConfig preyConfig) throws IOException {
		HttpPost method = new HttpPost(url);

		method.setHeader("Accept", "*/*");
		method.setEntity(new UrlEncodedFormEntity(getHttpParamsFromMap(params), HTTP.UTF_8));
		method.addHeader("Authorization", "Basic " + getCredentials(preyConfig.getApiKey(), "X"));

		// method.setParams(getHttpParamsFromMap(params));
		PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + params.toString());
		httpclient.setRedirectHandler(new NotRedirectHandler());
		HttpResponse httpResponse = httpclient.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		//PreyLogger.d("Response from server: " + response.toString());
		return response;
	}

	public PreyHttpResponse getAutentication(String url, Map<String, String> params, PreyConfig preyConfig) throws IOException {
		HttpPost method = new HttpPost(url);

		method.setHeader("Accept", "*/*");
		if(params!=null){
			method.setEntity(new UrlEncodedFormEntity(getHttpParamsFromMap(params), HTTP.UTF_8));
		}
		PreyLogger.i("apikey:"+preyConfig.getApiKey());
		method.addHeader("Authorization", "Basic " + getCredentials(preyConfig.getApiKey(), "X"));

		// method.setParams(getHttpParamsFromMap(params));
		PreyLogger.d("Sending using 'GET' - URI: " + url + " - parameters: " + (params!=null?params.toString():""));
		httpclient.setRedirectHandler(new NotRedirectHandler());
		HttpResponse httpResponse = httpclient.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		//PreyLogger.d("Response from server: " + response.toString());
		return response;
	}

	public PreyHttpResponse get(String url, Map<String, String> params, PreyConfig preyConfig) throws IOException {
		HttpGet method = new HttpGet(url + URLEncodedUtils.format(getHttpParamsFromMap(params), "UTF-8"));
		method.setHeader("Accept", "*/*");
		PreyLogger.d("Sending using 'GET' - URI: " + method.getURI());
		HttpResponse httpResponse = httpclient.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		//PreyLogger.d("Response from server: " + response.toString());
		return response;
	}

	public PreyHttpResponse get(String url, Map<String, String> params, PreyConfig preyConfig, String user, String pass) throws IOException {
		HttpGet method =null;
		if(params!=null){
			 method = new HttpGet(url + URLEncodedUtils.format(getHttpParamsFromMap(params), "UTF-8"));
		}else{
			 method = new HttpGet(url);
		}
		method.setHeader("Accept", "*/*");
		method.addHeader("Authorization", "Basic " + getCredentials(user, pass));
		//PreyLogger.d("Sending using 'GET' (Basic Authentication) - URI: " + method.getURI());
		HttpResponse httpResponse = httpclient.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		//PreyLogger.d("Response from server: " + response.toString());
		method.removeHeaders("Authorization");
		return response;
	}
	
	public PreyHttpResponse getAutentication2(String url, Map<String, String> params, PreyConfig preyConfig) throws IOException {
		HttpGet method =null;
		if(params!=null){
			 method = new HttpGet(url + URLEncodedUtils.format(getHttpParamsFromMap(params), "UTF-8"));
		}else{
			 method = new HttpGet(url);
		}
		method.setHeader("Accept", "*/*");
		PreyLogger.d("apikey:"+preyConfig.getApiKey());
		method.addHeader("Authorization", "Basic " + getCredentials(preyConfig.getApiKey(), "X"));
		//PreyLogger.d("Sending using 'GET' (Basic Authentication) - URI: " + method.getURI());
		HttpResponse httpResponse = httpclient.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		//PreyLogger.d("Response from server: " + response.toString());
		method.removeHeaders("Authorization");
		return response;
	}
	

	public PreyHttpResponse delete(String url, Map<String, String> params, PreyConfig preyConfig) throws IOException {
		HttpDelete method = new HttpDelete(url + URLEncodedUtils.format(getHttpParamsFromMap(params), "UTF-8"));
		method.setHeader("Accept", "*/*");
		method.addHeader("Authorization", "Basic " + getCredentials(preyConfig.getApiKey(), "X"));
		//PreyLogger.d("Sending using 'DELETE' (Basic Authentication) - URI: " + url + " - parameters: " + params.toString());
		HttpResponse httpResponse = httpclient.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		//PreyLogger.d("Response from server: " + response.toString());
		return response;
	}

	private PreyHttpResponse sendUsingMethodUsingCredentials(HttpPost method, PreyConfig preyConfig, String user, String pass) throws IOException {

		PreyHttpResponse response = null;
		try {
			// method.setDoAuthentication(true);
			method.addHeader("Authorization", "Basic " + getCredentials(user, pass));
			HttpResponse httpResponse = httpclient.execute(method);
			response = new PreyHttpResponse(httpResponse);
			//PreyLogger.d("Response from server: " + response.toString());

		} catch (IOException e) {
			PreyLogger.e("Error connecting with server", e);
			throw e;
		}
		return response;
	}

	private String getCredentials(String user, String password) {

		return (Base64.encodeBytes((user + ":" + password).getBytes()));
	}

	private PreyHttpResponse sendUsingMethod(HttpRequestBase method, PreyConfig preyConfig) throws IOException {

		PreyHttpResponse response = null;
		try {

			HttpResponse httpResponse = httpclient.execute(method);
			response = new PreyHttpResponse(httpResponse);
		//	//PreyLogger.d("Response from server: " + response.toString());

		} catch (IOException e) {
			throw e;
		}
		return response;
	}

	private String getUserAgent() {
		String userAgent= "Prey/".concat(PreyConfig.getPreyConfig(ctx).getPreyVersion()).concat(" (Android "+ PreyUtils.getBuildVersionRelease()+")") ;
		PreyLogger.d("userAgent:"+userAgent);
		return userAgent;
	}

	public String getStringUrl(String url,PreyConfig preyConfig) throws Exception {
		//PreyLogger.i("getStringUrl("+url+")");
		Map<String, String> parameters = new HashMap<String, String>();
		try {
			return PreyRestHttpClient.getInstance(ctx).get(url, parameters, preyConfig).getResponseAsString();
		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}
	}
	
	public PreyHttpResponse getDefault(String url) throws IOException {
		HttpGet method = new HttpGet(url);
		PreyLogger.d("Sending using 'GET' - URI: " + method.getURI());
		HttpResponse httpResponse = httpclientDefault.execute(method);
		PreyHttpResponse response = new PreyHttpResponse(httpResponse);
		//PreyLogger.d("Response from server: " + response.toString());
		return response;
	}
	
	public StringBuilder getStringHttpResponse(HttpResponse httpResponse) throws Exception {


		HttpEntity httpEntity = null;
		InputStream is = null;
		InputStreamReader input = null;
		BufferedReader reader = null;
		StringBuilder sb = null;
		try {
			httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();
			input = new InputStreamReader(is, "iso-8859-1");
			reader = new BufferedReader(input, 8);
			sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}

			sb.toString().trim();
		} catch (IllegalStateException e) {
			
		} catch (Exception e) {
			PreyLogger.e("Buffer Error, Error converting result " + e.toString(), e);
		} finally {
			try {
				if(is!=null)
					is.close();
			} catch (IOException e) {
			}
			try {
				if(reader!=null)
					reader.close();
			} catch (IOException e) {
			}
			try {
				if(input!=null)
					input.close();
			} catch (IOException e) {
			}
		}
		return sb;
	}
}

final class NotRedirectHandler implements RedirectHandler {

	public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
		return false;
	}

	public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
		return null;
	}
}
