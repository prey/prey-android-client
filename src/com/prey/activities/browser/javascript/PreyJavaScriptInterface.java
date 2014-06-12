package com.prey.activities.browser.javascript;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.accounts.UserEmail;
import com.prey.activities.browser.Login2BrowserActivity;
import com.prey.activities.browser.LoginBrowserActivity;
import com.prey.activities.browser.NewUserBrowserActivity;
import com.prey.activities.browser.PrePermissionBrowserActivity;
import com.prey.activities.browser.PermissionBrowserActivity;
import com.prey.activities.browser.javascript.action.LoginScriptInterface;
import com.prey.activities.browser.javascript.action.NewUserScriptInterface;
import com.prey.activities.browser.javascript.action.UnLockScriptInterface;
import com.prey.analytics.PreyGoogleAnalytics; 
import com.prey.beta.actions.PreyBetaController;
import com.prey.util.RUtil;
 

public class PreyJavaScriptInterface {

	private int wrongPasswordIntents = 0;

	private Context ctx;
	private String deviceType;
 

	public PreyJavaScriptInterface(Context ctx, String deviceType) {
		 
		this.ctx = ctx;
		this.deviceType = deviceType;
	}

	public void showToast(String toast) {
		Toast.makeText(ctx, toast, Toast.LENGTH_SHORT).show();
	}

	public void login(String email, String password) {
		LoginScriptInterface login = new LoginScriptInterface(ctx);
		login.execute(email, password,deviceType, wrongPasswordIntents);
	}
	
	public void userRegistered(String password) {
		try{
			PreyBetaController.startPrey(ctx);
		}catch(Exception e){
			PreyLogger.e("Error, causa:"+e.getMessage(), e);
		}
		LoginScriptInterface login = new LoginScriptInterface(ctx);
		String email = PreyConfig.getPreyConfig(ctx).getEmail();
		login.execute(email, password,deviceType, wrongPasswordIntents);
	}
	

	public void newuser(String name, String email, String password) {
		NewUserScriptInterface newUser = new NewUserScriptInterface(ctx);
		newUser.execute(name, email, password, password, deviceType);
	}

	public void goNewUser() {
		Intent intent = new Intent(ctx, NewUserBrowserActivity.class);
		ctx.startActivity(intent);
	}

	public void goPanel() {
 
	}

	public void goLogin() {
		Intent intent = new Intent(ctx, LoginBrowserActivity.class);
		ctx.startActivity(intent);
	}
	
	public void loginPanel() {
		Intent intent = new Intent(ctx, Login2BrowserActivity.class);
		ctx.startActivity(intent);
	}
	
	public void configureSimHtml(String str) {
		Toast.makeText(ctx, "Configure Sim:" + str, Toast.LENGTH_SHORT).show();

	}

	public void onUnlockPass(boolean unlockPass) {
		UnLockScriptInterface unlockInterface = new UnLockScriptInterface(ctx);
		unlockInterface.execute(unlockPass);
	}
	
	public String loginMail(){
		String email = UserEmail.getEmail(ctx);
		return email;
	}

	//Next Method Delete 
	
	public void activeCamouflageHtml(String str) {
		Toast.makeText(ctx, "Active Camouflage:" + str, Toast.LENGTH_SHORT).show();

	}

	public void uninstallLockHtml(String str) {
		Toast.makeText(ctx, "Uninstall Lock:" + str, Toast.LENGTH_SHORT).show();
	}
	
	public void permission(){
		Intent intent = new Intent(ctx, PermissionBrowserActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ctx.startActivity(intent);
		 
	}
	
	public void prePermission(){
		Intent intent = new Intent(ctx, PrePermissionBrowserActivity.class);
		ctx.startActivity(intent);
		 
	}
	
	public void preyOn(){
	 	 
		PreyBetaController.startPrey(ctx);
 
	}
	
	public void startPage(String page){
		PreyLogger.i("startPage:"+page);
		PreyGoogleAnalytics.getInstance().trackAsynchronously(ctx, page);
	}
	
	
	public void openPanel(){
		String url = "https://www.preyproject.com/login";
		PreyLogger.i("openPanel:"+url);		
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ctx.startActivity(i);
		 
	}
	
	
	public String textStringXml(String id){
		String text=ctx.getText(RUtil.idStringXml(id)).toString();
		//PreyLogger.i("id["+id+"]["+text+"]");		
		return text;
	}
}
