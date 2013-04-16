package com.prey.net.http.factory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine; 
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

public class ReportHttpResponse implements HttpResponseType{

 
	
	public HttpResponse execute(HttpRequest request,Map<String,String> map){
		int statusCode=201;
		String reasonPhrase="OK";
		ProtocolVersion protocolVersion=new ProtocolVersion("HTTP", 1, 1);
		StatusLine statusLine=new BasicStatusLine(protocolVersion, statusCode, reasonPhrase);
		HttpResponse response=new BasicHttpResponse(statusLine);
		BasicHttpEntity httpEntity =new BasicHttpEntity();
		String htmlTxt="[]";
		InputStream stream = new ByteArrayInputStream(htmlTxt.getBytes());
		httpEntity.setContent(stream);
		response.setEntity(httpEntity);
		return response;
	}

}
