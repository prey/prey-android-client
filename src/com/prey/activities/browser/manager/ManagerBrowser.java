package com.prey.activities.browser.manager;

import com.prey.PreyConfig;
 
 
import com.prey.activities.browser.TourBrowserActivity;
 
import com.prey.activities.browser.ReadyBrowserActivity;
import com.prey.activities.browser.UserRegisteredBrowserActivity;
import com.prey.activities.browser.WarningBrowserActivity;
import com.prey.backwardcompatibility.FroyoSupport;
 
 

import android.content.Context;
import android.content.Intent;

public class ManagerBrowser {

	
	
	public void preLogin(Context ctx){
		Intent intent = null;
		if (PreyConfig.getPreyConfig(ctx).isActiveTour()){
			 intent = new Intent(ctx, TourBrowserActivity.class);
		}else{
			 if (PreyConfig.getPreyConfig(ctx).isActiveWizard()){
				 intent = new Intent(ctx, WarningBrowserActivity.class);
			 }else{
				 intent = new Intent(ctx, ReadyBrowserActivity.class);
			 }
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	 
		ctx.startActivity(intent);
		
	}
	
	public void postLogin(Context ctx){
		Intent intent =null;
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		if (preyConfig.isFroyoOrAbove() && !FroyoSupport.getInstance(ctx).isAdminActive()){
			 intent = new Intent(ctx, WarningBrowserActivity.class);
		}else{
        	 intent = new Intent(ctx, ReadyBrowserActivity.class);
 		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		ctx.startActivity(intent);
		
	}
	
	public void thisDeviceAlreadyRegistered(Context ctx){
		Intent intent = null;
		intent = new Intent(ctx, UserRegisteredBrowserActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}
}
