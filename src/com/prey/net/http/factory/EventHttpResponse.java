package com.prey.net.http.factory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;

import com.prey.PreyLogger;

public class EventHttpResponse implements HttpResponseType {

	public HttpResponse execute(HttpRequest request, Map<String, String> map) {

		if (request instanceof HttpPost) {
			HttpPost post = (HttpPost) request;
			try {
				String stringifiedResponse = EntityUtils.toString(post.getEntity());
				PreyLogger.i("stringifiedResponse:" + stringifiedResponse);
			} catch (Exception e) {
				PreyLogger.e("Error, causa:" + e.getMessage(), e);
			}
		}

		int statusCode = 200;
		String reasonPhrase = "OK";
		ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
		StatusLine statusLine = new BasicStatusLine(protocolVersion, statusCode, reasonPhrase);
		HttpResponse response = new BasicHttpResponse(statusLine);
		BasicHttpEntity httpEntity = new BasicHttpEntity();
		String htmlTxt = "[]";
		InputStream stream = new ByteArrayInputStream(htmlTxt.getBytes());
		httpEntity.setContent(stream);
		response.setEntity(httpEntity);
		return response;
	}
}
