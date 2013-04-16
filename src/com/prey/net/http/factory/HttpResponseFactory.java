package com.prey.net.http.factory;

import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse; 
 

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
	
	public HttpResponse execute(HttpRequest request,Map<String,String> map){
		String url="";
		try{
			url=""+request.getRequestLine().getUri();
		}catch(Exception e){
			
		}
		HttpResponseType type=null;
		if (url.indexOf(".json")>=0){
			type= new  JsonHttpResponse();
		}
		if (url.indexOf("/reports")>=0){
			type= new  ReportHttpResponse();
		}
		if (url.indexOf("/data")>=0){
			type= new  DataHttpResponse();
		}
		if (url.indexOf("/events")>=0){
			type= new  EventHttpResponse();
		}
 
		
		return type.execute(request,map);
	}

}
