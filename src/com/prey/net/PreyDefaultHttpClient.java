package com.prey.net;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import com.prey.PreyLogger;

public class PreyDefaultHttpClient {

	public void setRedirectHandler(RedirectHandler redirectHandler) {
		this.client.setRedirectHandler(redirectHandler);
	}

	public void setParams(HttpParams params) {
		this.client.setParams(params);
	}

	private DefaultHttpClient client;

	private int MAX_RETRIES = 5;
	private int STATUS_CODE_503 = 503;

	public PreyDefaultHttpClient(DefaultHttpClient client) {
		this.client = client;
	}

	public HttpResponse execute(HttpRequestBase base) throws ClientProtocolException, IOException {
		HttpResponse response = null;
		int count = 0;
		do {
			try {
				response = client.execute(base);
				PreyLogger.i("[" + count + "]base:" + base.getURI() +"{" + response.getStatusLine().getStatusCode()+"}");
			} catch (ConnectTimeoutException e) {
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			}
			if (response == null) {
				count++;
			} else {
				if (response.getStatusLine().getStatusCode() != STATUS_CODE_503) {
					count = MAX_RETRIES;
				} else {
					count++;
					sleep();
				}
			}
		} while (count < MAX_RETRIES);
		return response;
	}

	public HttpResponse execute(HttpPost httpPost) throws ClientProtocolException, IOException {
		HttpResponse response = null;
		int count = 0;
		do {
			try {
				response = client.execute(httpPost);
				PreyLogger.i("[" + count + "]post:" + httpPost.getURI() +"{" + response.getStatusLine().getStatusCode()+"}");
			} catch (ConnectTimeoutException e) {
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			}
			if (response == null) {
				count++;
			} else {
				if (response.getStatusLine().getStatusCode() != STATUS_CODE_503) {
					count = MAX_RETRIES;
				} else {
					count++;
					sleep();
				}
			}
		} while (count < MAX_RETRIES);
		return response;
	}

	public HttpResponse execute(HttpGet httpGet) throws ClientProtocolException, IOException {
		HttpResponse response = null;
		int count = 0;
		do {
			try {
				response = client.execute(httpGet);
				PreyLogger.i("[" + count + "]get:" + httpGet.getURI() +"{" + response.getStatusLine().getStatusCode()+"}");
			} catch (ConnectTimeoutException e) {
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			}
			if (response == null) {
				count++;
			} else {
				if (response.getStatusLine().getStatusCode() != STATUS_CODE_503) {
					count = MAX_RETRIES;
				} else {
					count++;
					sleep();
				}
			}
		} while (count < MAX_RETRIES);
		return response;
	}

	public HttpResponse execute(HttpPut httpPut) throws ClientProtocolException, IOException {
		HttpResponse response = null;
		int count = 0;
		do {
			try {
				response = client.execute(httpPut);
				PreyLogger.i("[" + count + "]put:" + httpPut.getURI() +"{" + response.getStatusLine().getStatusCode()+"}");
			} catch (ConnectTimeoutException e) {
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			}
			if (response == null) {
				count++;
			} else {
				if (response.getStatusLine().getStatusCode() != STATUS_CODE_503) {
					count = MAX_RETRIES;
				} else {
					count++;
					sleep();
				}
			}
		} while (count < MAX_RETRIES);
		return response;
	}

	public HttpResponse execute(HttpDelete httpDelete) throws ClientProtocolException, IOException {
		HttpResponse response = null;
		int count = 0;
		do {
			try {
				response = client.execute(httpDelete);
				PreyLogger.i("[" + count + "]delete:" + httpDelete.getURI() +"{" + response.getStatusLine().getStatusCode()+"}");
			} catch (ConnectTimeoutException e) {
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			}
			if (response == null) {
				count++;
			} else {
				if (response.getStatusLine().getStatusCode() != STATUS_CODE_503) {
					count = MAX_RETRIES;
				} else {
					count++;
					sleep();
				}
			}
		} while (count < MAX_RETRIES);
		return response;
	}
	
	public void sleep(){
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
	}

}
