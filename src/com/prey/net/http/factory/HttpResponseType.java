package com.prey.net.http.factory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public interface HttpResponseType {

	public HttpResponse execute(HttpUriRequest request);
}
