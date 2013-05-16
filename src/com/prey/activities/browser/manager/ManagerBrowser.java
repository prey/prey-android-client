package com.prey.activities.browser.manager;

import com.prey.PreyConfig;
import com.prey.activities.browser.InstallBrowserActivity;
import com.prey.activities.browser.LoginBrowserActivity;
import com.prey.activities.browser.PanelBrowserActivity;
import com.prey.activities.browser.UserRegisteredBrowserActivity;
import com.prey.activities.browser.WizardBrowserActivity;

import android.content.Context;
import android.content.Intent;

public class ManagerBrowser {

	
	
	public void preLogin(Context ctx){
		Intent intent = null;
		if (PreyConfig.getPreyConfig(ctx).isActiveTour()){
			 intent = new Intent(ctx, InstallBrowserActivity.class);
		}else{
			 intent = new Intent(ctx, LoginBrowserActivity.class);
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}
	
	public void postLogin(Context ctx){
	
		Intent intent =null;
		if (PreyConfig.getPreyConfig(ctx).isActiveWizard()){
			intent = new Intent(ctx, WizardBrowserActivity.class);
		}else{
			intent = new Intent(ctx, PanelBrowserActivity.class);
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
