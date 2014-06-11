package com.prey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.prey.actions.HttpDataService;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyRestHttpClient;
import com.prey.net.PreyWebServices;
import com.prey.net.http.EntityFile;

public class PreyEmail {

	
	public static void sendDataMail(Context ctx, HttpDataService data) {
		try {
			if (data!=null ){
				List<EntityFile> entityFiles = data.getEntityFiles();
				if(entityFiles!=null&&entityFiles.size()>=0){ 
					String url = PreyWebServices.getInstance().getFileUrlJson(ctx);
					PreyLogger.d("URL:" + url);
					Map<String, String> parameters = new HashMap<String, String>();
					PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
					PreyHttpResponse preyHttpResponse = null;
					preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters, preyConfig, entityFiles);
					PreyLogger.i("status line:" + preyHttpResponse.getStatusLine());
				}
			}
		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public static String getEmail(Context context) {
	     AccountManager accountManager = AccountManager.get(context); 
		 Account account = getAccount(accountManager);

		    if (account == null) {
		      return null;
		    } else {
		      return account.name;
		    }
	}
	
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private static Account getAccount(AccountManager accountManager) {
	    Account[] accounts = accountManager.getAccountsByType("com.google");
	    Account account;
	    if (accounts.length > 0) {
	      account = accounts[0];      
	    } else {
	      account = null;
	    }
	    return account;
	}
}
