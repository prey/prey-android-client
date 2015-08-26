package com.prey.actions;

/**
 * Created by oso on 24-08-15.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prey.net.http.EntityFile;



public class HttpDataService {

    private String key;
    private HashMap<String, String> dataList;
    private String singleData;
    private boolean isList;
    private String httpMethod;
    private String url;
    private List<EntityFile> entityFiles;

    public void addDataListAll(HashMap<String, String> map) {
        dataList.putAll(map);
    }

    public HttpDataService(String key) {
        this.key = key;
        dataList = new HashMap<String, String>();
        entityFiles= new ArrayList<EntityFile>();
    }

    public HashMap<String, String> getReportAsParameters() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        if (isList()) {
            StringBuffer key = new StringBuffer();
            for (String valueKey : dataList.keySet()) {
                String valueData = dataList.get(valueKey);
                key.append("");
                key.append(this.key);
                key.append("[");
                key.append(valueKey);
                key.append("]");
                parameters.put(key.toString(), valueData);
                key.delete(0, key.length());
            }
            key = null;
        } else
            parameters.put(" "+key+" ", singleData);

        return parameters;
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

    public void putData(Map<String, String> dataList){
        this.dataList.putAll(dataList);
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


    public void addEntityFile(EntityFile entityFile) {
        entityFiles.add(entityFile);
    }

    public List<EntityFile> getEntityFiles() {
        return entityFiles;
    }

    public void setEntityFiles(List<EntityFile> entityFiles) {
        this.entityFiles = entityFiles;
    }





}
