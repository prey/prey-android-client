package com.prey.twilio;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import com.prey.PreyLogger;

 

public class HttpHelper {

	 private static HttpClient httpClient;

	    private static void ensureHttpClient()
	    {
	        if (httpClient != null)
	            return;

	        BasicHttpParams params = new BasicHttpParams();
	        HttpConnectionParams.setConnectionTimeout(params, 45000);
	        HttpConnectionParams.setSoTimeout(params, 30000);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", new PlainSocketFactory(), 80));
	        try {
	            registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
	        } catch (Exception e) {
	           PreyLogger.e( "Unable to register HTTPS socket factory: " + e.getLocalizedMessage(),e);
	        }

	        ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(params, registry);
	        httpClient = new DefaultHttpClient(connManager, params);
	    }

	    private static String stringFromInputStream(InputStream is) throws IOException
	    {
	        char[] buf = new char[1024];
	        StringBuilder out = new StringBuilder();

	        Reader in = new InputStreamReader(is, "UTF-8");

	        int bin;
	        while ((bin = in.read(buf, 0, buf.length)) >= 0) {
	            out.append(buf, 0, bin);
	        }

	        return out.toString();
	    }

	    public static String httpGet(String url) throws Exception
	    {
	        ensureHttpClient();

	        HttpGet request = new HttpGet(url);
	        HttpResponse response = httpClient.execute(request);
	        if (response != null)
	        {
	            int statusCode = response.getStatusLine().getStatusCode();
	            if (statusCode == 200)
	                return stringFromInputStream(response.getEntity().getContent());
	            else
	                throw new Exception("Got error code " + statusCode + " from server");
	        }

	        throw new Exception("Unable to connect to server");
	    }
}
