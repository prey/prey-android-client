package com.prey.net;

import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLException;

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

	private int MAX_RETRIES = 4;
	private int STATUS_CODE_503 = 503;


	public PreyDefaultHttpClient(DefaultHttpClient client) {
		this.client = client;
	}

	public HttpResponse execute(HttpRequestBase base) throws ClientProtocolException, IOException {
		HttpResponse response = null;
		int count = 0;
		do {
			try {
				PreyLogger.d("[" + count + "]ini base:" + base.getURI());
				response = client.execute(base);
				if (response!=null)
					PreyLogger.d("[" + count + "]base:" + base.getURI() +"{" + response.getStatusLine().getStatusCode()+"}");
			} catch (ConnectTimeoutException e) {
				PreyLogger.d("[" + count + "]base ConnectTimeoutException:");
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			} catch (SocketTimeoutException e) {
				PreyLogger.d("[" + count + "]base SocketTimeoutException:");
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			} catch (SSLException e) {
				PreyLogger.d("[" + count + "]base SSLException:");
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
					return response;
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
				PreyLogger.d("[" + count + "]ini post:" + httpPost.getURI());
				response = client.execute(httpPost);
				if (response!=null)
					PreyLogger.d("[" + count + "]post:" + httpPost.getURI() +"{" + response.getStatusLine().getStatusCode()+"}");
			} catch (ConnectTimeoutException e) {
				PreyLogger.d("[" + count + "]post ConnectTimeoutException:");
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			} catch (SocketTimeoutException e) {
				PreyLogger.d("[" + count + "]post SocketTimeoutException:");
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			} catch (SSLException e) {
				PreyLogger.d("[" + count + "]post SSLException:");
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
					return response;
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
				PreyLogger.d("[" + count + "]ini get:" + httpGet.getURI());
				response = client.execute(httpGet);
				if (response!=null)
					PreyLogger.d("[" + count + "]get:" + httpGet.getURI() +"{" + response.getStatusLine().getStatusCode()+"}");
			} catch (ConnectTimeoutException e) {
				PreyLogger.d("[" + count + "]get ConnectTimeoutException:");
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			} catch (SocketTimeoutException e) {
				PreyLogger.d("[" + count + "]get SocketTimeoutException:");
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			} catch (SSLException e) {
				PreyLogger.d("[" + count + "]get SSLException:");
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
					return response;
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
				PreyLogger.d("[" + count + "]ini put:" + httpPut.getURI());
				response = client.execute(httpPut);
				if (response!=null)
					PreyLogger.d("[" + count + "]put:" + httpPut.getURI() +"{" + response.getStatusLine().getStatusCode()+"}");
			} catch (ConnectTimeoutException e) {
				PreyLogger.d("[" + count + "]put ConnectTimeoutException:");
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			} catch (SocketTimeoutException e) {
				PreyLogger.d("[" + count + "]put SocketTimeoutException:");
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			} catch (SSLException e) {
				PreyLogger.d("[" + count + "]put SSLException:");
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
					return response;
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
				PreyLogger.d("[" + count + "]ini delete:" + httpDelete.getURI());
				response = client.execute(httpDelete);
				if (response!=null)
					PreyLogger.d("[" + count + "]delete:" + httpDelete.getURI() +"{" + response.getStatusLine().getStatusCode()+"}");
			} catch (ConnectTimeoutException e) {
				PreyLogger.d("[" + count + "]delete ConnectTimeoutException:");
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			} catch (SocketTimeoutException e) {
				PreyLogger.d("[" + count + "]delete SocketTimeoutException:");
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			} catch (SSLException e) {
				PreyLogger.d("[" + count + "]delete SSLException:");
				if (count < MAX_RETRIES) {
					response = null;
				} else {
					throw e;
				}
			}
			
			if (response == null) {
				count++;
			} else {
				PreyLogger.d("[" + count + "]delete:" + httpDelete.getURI());
				if (response.getStatusLine().getStatusCode() != STATUS_CODE_503) {
					return response;
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
