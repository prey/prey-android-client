package com.prey.activities.javascript;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.activities.WelcomeActivity;

public class PreyJavaScriptInterface {
	 Context ctx;

	private WelcomeActivity welcome;
	public void setActivity(WelcomeActivity welcome) {
		this.welcome = welcome;
	}

	    /** Instantiate the interface and set the context */
	    public PreyJavaScriptInterface(Context ctx) {
	        this.ctx = ctx;
	    }

	    /** Show a toast from the web page */

	    public void showToast(String toast) {
	        Toast.makeText(ctx, toast, Toast.LENGTH_SHORT).show();
	    }


		public void menu(){

			Intent intent  = new Intent(ctx, WelcomeActivity.class);
			ctx.startActivity(intent);

			PreyConfig.getPreyConfig(ctx).setProtectTour(true);
			welcome.finish();


		}
}
