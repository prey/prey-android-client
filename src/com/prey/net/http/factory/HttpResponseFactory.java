package com.prey.net.http.factory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public class HttpResponseFactory {

	
	private static HttpResponseFactory instance=null;
	
	private HttpResponseFactory(){
		
	}
	
	public static HttpResponseFactory getInstance(){
		if (instance==null){
			instance=new HttpResponseFactory();
		}
		return instance;
	}
	
	public HttpResponse execute(HttpUriRequest request){
		String url="";
		try{
			url=""+request.getURI().toURL();
		}catch(Exception e){
			
		}
		HttpResponseType type=null;
		if (url.indexOf(".json")>=0){
			type= new  JsonHttpResponse();
		}
		return type.execute(request);
	}

}
