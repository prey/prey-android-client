/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

public class FileConfigReader {

	private static FileConfigReader _instance = null;
	Properties properties;
	
	private FileConfigReader(Context ctx) {
		try {
			PreyLogger.d("Loading config properties from file...");
			properties = new Properties();
			InputStream is = ctx.getResources().openRawResource(R.raw.config);
		    properties.load(is);
		    is.close();
		    PreyLogger.d("Config: "+properties);
			
		} catch (NotFoundException e) {
		    PreyLogger.e("Config file wasn't found", e);
	    } catch (IOException e) {
	    	 PreyLogger.e("Couldn't read config file", e);
	    }
	}
	
	public static FileConfigReader getInstance(Context ctx){
		if (_instance == null)
			_instance = new FileConfigReader(ctx);
		return _instance;
		
	}
	
	public String getAgreementId(){
		return properties.getProperty("agreement-id");
	}
	public String getGcmId(){
		return properties.getProperty("gcm-id");
	}
	public String getGcmIdPrefix(){
		return properties.getProperty("gcm-id-prefix");
	}
	
	public String getc2dmAction(){
		return properties.getProperty("c2dm-action");
	}
	public String getc2dmMessageSync(){
		return properties.getProperty("c2dm-message-sync");
	}
	
	public String getPreyDomain(){
		return properties.getProperty("prey-domain");
	}
	
	public String getPreySubdomain(){
		return properties.getProperty("prey-subdomain");
	}
	
	public String getPreyUiSubdomain(){
		return properties.getProperty("prey-ui-subdomain");
	}
	
	public String getPreySupportSubdomain(){
		return properties.getProperty("prey-answers");
	}
	
	public String getPreyAnswersSubdomain(){
		return properties.getProperty("prey-support");
	}

	public String getPreyMinorVersion(){
		return properties.getProperty("prey-minor-version");
	}

	public boolean isAskForPassword() {
		return Boolean.parseBoolean(properties.getProperty("ask-for-password"));
	}
	public boolean isLogEnabled() {
		return Boolean.parseBoolean(properties.getProperty("log-enabled"));
	}

	public String getAnalyticsUA() {
		return properties.getProperty("analytics-ua");
	}
	
	public String getApiV1(){
		return properties.getProperty("api-v1");
	}
 
	public String getApiV2(){
		return properties.getProperty("api-v1");
	}
	
}
