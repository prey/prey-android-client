package com.prey.activities.browser.javascript.action;

import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.activities.browser.PermissionBrowserActivity;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NewUserScriptInterface {

	
	private Context ctx = null;

	public NewUserScriptInterface(Context ctx) {
		this.ctx = ctx;
	}
	
	public void execute(String name,String email,String password,String repassword,String deviceType){
		PreyLogger.i("newuser("+name+","+email+","+password+","+repassword+")");
    	String error="";
    	if (name.equals("") || email.equals("") || password.equals(""))
			Toast.makeText(ctx, R.string.error_all_fields_are_required, Toast.LENGTH_LONG).show();
		else if (!password.equals(repassword))
			Toast.makeText(ctx, R.string.preferences_passwords_do_not_match, Toast.LENGTH_LONG).show();
		else {
			try {
				PreyAccountData accountData = PreyWebServices.getInstance().registerNewAccount(ctx, name, email,password,deviceType);
				PreyLogger.d("Response creating account: " + accountData.toString());
				PreyConfig.getPreyConfig(ctx).saveAccount(accountData);
				Intent intent = new Intent(ctx, PermissionBrowserActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			ctx.startActivity(intent);
			} catch (PreyException e) {
				error = e.getMessage();
				Toast.makeText(ctx,error, Toast.LENGTH_LONG).show();				
			}
		}
	}
}
