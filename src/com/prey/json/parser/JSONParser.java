package com.prey.json.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;

public class JSONParser {
	InputStream is;
	JSONObject jObj;
	String json;
	boolean error;

	private final static String COMMAND = "\"command\"";

	// constructor
	public JSONParser() {

	}

	public List<JSONObject> getJSONFromUrl(Context ctx, String url) {
		is = null;
		error = false;
		PreyLogger.d("url:" + url);
		// Making HTTP request
		try {

			// defaultHttpClient
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI(url));
			HttpResponse httpResponse = client.execute(request);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();
		} catch (Exception e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
			error = true;
		}

		if (!error) {
			InputStreamReader input = null;
			BufferedReader reader = null;
			StringBuilder sb = null;
			try {
				input = new InputStreamReader(is, "iso-8859-1");
				reader = new BufferedReader(input, 8);
				sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}

				json = sb.toString().trim();

			} catch (Exception e) {
				PreyLogger.e("Buffer Error, Error converting result " + e.toString(), e);
				error = true;
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
				try {
					reader.close();
				} catch (IOException e) {
				}
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}

		// json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";

		// json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"screenshot\",\"picture\",\"location\"]}}]";

		// json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"screenshot\"]}}]";

		// json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"location\"]}}]";

		// json="[ {\"command\": \"start\",\"target\": \"geofencing\",\"options\": {\"origin\": \"-70.60713481,-36.42372147\",\"radius\": \"100\" }}]";

		// json="[ {\"command\": \"start\",\"target\": \"geofencing\",\"options\": {\"origin\": \"-70.7193117,-32.7521112\",\"radius\": \"100\" }}]";

		// json="[ {\"command\": \"start\",\"target\": \"geofencing\",\"options\": {\"origin\": \"-70.60713481,-33.42372147\",\"radius\": \"100\" }}]";
		// json="[ {\"command\": \"stop\",\"target\": \"geofencing\",\"options\": {}}]";

		// json="[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}},{\"command\":\"start\",\"target\":\"alarm\",\"options\":null}, {\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\"[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";

		// json="[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}}, {\"command\":\"get\",\"target\":\"report\",\"options\":{\"delay\": \"25\",\"include\"[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";

		//json = "[ {\"command\": \"data\",\"target\": \"location\",\"options\": {}}]";

		if ("[]".equals(json)) {
			return null;
		}
		return getJSONFromTxt(ctx, json);
	}

	public List<JSONObject> getJSONFromTxt(Context ctx, String json) {
		jObj = null;
		List<JSONObject> listaJson = new ArrayList<JSONObject>();
		List<String> listCommands = getListCommands(json);
		for (int i = 0; listCommands != null && i < listCommands.size(); i++) {
			String command = listCommands.get(i);
			try {
				jObj = new JSONObject(command);
				listaJson.add(jObj);
			} catch (JSONException e) {
				PreyLogger.e("JSON Parser, Error parsing data " + e.toString(), e);
			}
		}
		PreyLogger.i("json:" + json);
		// return JSON String
		return listaJson;
	}

	private List<String> getListCommands(String json) {
		json = json.replaceAll("nil", "{}");
		json = json.replaceAll("null", "{}");
		List<String> lista = new ArrayList<String>();
		int posicion = json.indexOf(COMMAND);
		json = json.substring(posicion + 9);
		posicion = json.indexOf(COMMAND);
		String command = "";
		while (posicion > 0) {
			command = json.substring(0, posicion);
			json = json.substring(posicion + 9);
			lista.add("{" + COMMAND + cleanChar(command));
			posicion = json.indexOf("\"command\"");
		}
		lista.add("{" + COMMAND + cleanChar(json));
		return lista;
	}

	private String cleanChar(String json) {
		if (json != null) {
			json = json.trim();
			char c = json.charAt(json.length() - 1);
			while (c == '{' || c == ',' || c == ']') {
				json = json.substring(0, json.length() - 1);
				json = json.trim();
				c = json.charAt(json.length() - 1);
			}
		}
		return json;
	}
}
