/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions;

 


import android.content.Context; 

public class ActionsRunnner {

	protected boolean running = false;

	Thread myActionsRunnerThread = null;

	public ActionsRunnner() {

	}

	public void run(Context ctx) {
		this.myActionsRunnerThread = new Thread(new ActionsRunner(ctx));
		this.myActionsRunnerThread.start();

	}
 
}
