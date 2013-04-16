package com.prey.net.http.factory;

import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse; 

public interface HttpResponseType {

	public HttpResponse execute(HttpRequest request,Map<String,String> map);
}
