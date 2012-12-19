package com.prey.json.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.prey.PreyLogger;

public class JSONParser {
	 static InputStream is = null;
	    static JSONObject jObj = null;
	    static String json = "";
	 
	    
	    // constructor
	    public JSONParser() {
	 
	    }
	 
	    public JSONObject getJSONFromUrl(String url) {
	    	  PreyLogger.i("url:"+url);
	        // Making HTTP request
	        try {
	            // defaultHttpClient
	        	HttpClient client = new DefaultHttpClient();
	            HttpGet request  = new HttpGet();
	            request.setURI(new URI(url));
	        
	            HttpResponse httpResponse = client.execute(request);
	            HttpEntity httpEntity = httpResponse.getEntity();
	            is = httpEntity.getContent();          
	 
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 
	        try {
	            BufferedReader reader = new BufferedReader(new InputStreamReader(
	                    is, "iso-8859-1"), 8);
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	            is.close();
	            
	            
	            json = sb.toString().trim();
	          
	           //json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";
	         //   json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"screenshot\",\"picture\",\"location\"]}}]";
	            
	           // json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"location\"]}}]";
		          
//	           json="[ {\"command\": \"start\",\"target\": \"geofencing\",\"options\": {\"origin\": \"-70.60713481,-36.42372147\",\"radius\": \"100\" }}]";
	            
	            
	         
	            
	       //    json="[ {\"command\": \"start\",\"target\": \"geofencing\",\"options\": {\"origin\": \"-70.60713481,-33.42372147\",\"radius\": \"100\" }}]";
	       //    json="[ {\"command\": \"stop\",\"target\": \"geofencing\",\"options\": {}}]";
	            
	            
	            if ("[]".equals(json)){
	            	return null;
	            }
	            PreyLogger.i("json:"+json);
	            // json=json.replaceAll("\\\\", "");
	            //  json=json.replaceAll(":", "\"");
	            //         json=json.replaceAll("=>", "\":");
	            
	    //        json=json.replaceAll("\"\"", "\"");
	            
	            //    json=json.replaceAll("nil", "{}");
	            json=json.replaceAll("nil", "{}");
	            json=json.replaceAll("null", "{}");
	            json=json.substring(1);
	            json=json.substring(0,json.length()-1);

	            
	            PreyLogger.i("json:"+json);
	        } catch (Exception e) {
	        	PreyLogger.e("Buffer Error, Error converting result " + e.toString(),e);
	        }
	        
	      //  json="{ 	\"target\": \"get\", 	\"name\": \"report\", 	\"options\": { 		\"include\": [ \"screenshot\", \"picture\", \"location\"], 		\"interval\": \"10\" 	}	 }";
	 
	        // try parse the string to a JSON object
	        try {
	            jObj = new JSONObject(json);
	        } catch (JSONException e) {
	        	PreyLogger.e("JSON Parser, Error parsing data " + e.toString(),e);
	        }
	 
	        // return JSON String
	        return jObj;
	 
	    }
}
