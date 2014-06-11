/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.actions;

 


import android.content.Context; 

public class PreyBetaActionsRunnner {

	protected boolean running = false;

	Thread myActionsRunnerThread = null;

	public PreyBetaActionsRunnner() {

	}

	public void run(Context ctx) {
		this.myActionsRunnerThread = new Thread(new PreyBetaActionsRunner(ctx));
		this.myActionsRunnerThread.start();

	}
 
}
