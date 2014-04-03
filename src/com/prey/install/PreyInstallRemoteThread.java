package com.prey.install;

import android.content.Context;
import android.os.Build;

 

import com.prey.PreyConfig;
import com.prey.PreyEmail;
import com.prey.PreyLogger; 
import com.prey.PreyUtils;
import com.prey.backwardcompatibility.AboveCupcakeSupport;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

public class PreyInstallRemoteThread extends Thread {
	private Context ctx;

	public PreyInstallRemoteThread(Context ctx) {
		this.ctx = ctx;
	}

	public void run() {
		try {
			String email = PreyEmail.getEmail(ctx);
			String notificationId = PreyConfig.getPreyConfig(ctx).getNotificationId();
			String model = Build.MODEL;
			String vendor = "Google";
			if (!PreyConfig.getPreyConfig(ctx).isCupcake())
				vendor = AboveCupcakeSupport.getDeviceVendor();
 	
			PreyLogger.d("email:"+email+" vendor:"+vendor+" model:"+model+" notificationId:"+notificationId);
			
			if (notificationId!=null&&!"".equals(notificationId)){
				String deviceType = PreyUtils.getDeviceType(ctx);
				PreyHttpResponse response=PreyWebServices.getInstance().registerNewDeviceRemote(ctx, email,notificationId, deviceType);
				PreyLogger.d("remote install response:"+response.getResponseAsString());
				PreyLogger.d("remote install code:"+response.getStatusLine().getStatusCode());

				if (response.getStatusLine().getStatusCode()==200){
					PreyConfig.getPreyConfig(ctx).setSendNotificationId(true); 
				}
			}
		} catch (Exception e) {
			PreyLogger.i("Error remote install: " + e.getMessage());

		}
	}

}
