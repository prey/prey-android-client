/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions;

 
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

 

public class HttpDataService {

	private String key;
	private HashMap<String, String> dataList;
	private String singleData;
	private boolean isList;
	private String httpMethod;
	private String url;
	private List<InputStream> files;
 

	public HttpDataService(String key) {
		this.key = key;
		dataList = new HashMap<String, String>();
		files= new ArrayList<InputStream>();
	}

	public HashMap<String, String> getDataAsParameters() {
		HashMap<String, String> parameters = new HashMap<String, String>();
		if (isList()) {
			StringBuffer key = new StringBuffer();
			for (String valueKey : dataList.keySet()) {
				String valueData = dataList.get(valueKey);
				key.append(this.key);
				key.append("[");
				key.append(valueKey);
				key.append("]");
				parameters.put(key.toString(), valueData);
				key.delete(0, key.length());
			}
			key = null;
		} else
			parameters.put(key, singleData);

		return parameters;
	}

	public String getDataAsString() {
		StringBuffer sb = new StringBuffer();

		if (isList()) {
			for (String valueKey : dataList.keySet()) {
				String valueData = dataList.get(key);
				sb.append(key);
				sb.append("[");
				sb.append(valueKey);
				sb.append("]=");
				sb.append(valueData);
				sb.append("&");
			}

		} else
			sb.append(key).append("=").append(singleData).append("&");
		return sb.toString();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public HashMap<String, String> getDataList() {
		return dataList;
	}

	public void setDataList(HashMap<String, String> dataList) {
		this.dataList = dataList;
	}

	public String getSingleData() {
		return singleData;
	}

	public void setSingleData(String singleData) {
		this.singleData = singleData;
	}

	public boolean isList() {
		return isList;
	}

	public void setList(boolean isList) {
		this.isList = isList;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<InputStream> getFiles() {
		return files;
	}

	public void setFiles(List<InputStream> files) {
		this.files = files;
	}

	public void addFile(InputStream file) {
		files.add(file);
	}


 
 

}
