/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;

import com.prey.PreyException;
import com.prey.actions.location.LocationNotifierAction;
import com.prey.actions.observer.ActionJob;

public abstract class PreyAction {
	
	public final String ID = "";
	private static HashMap<String, PreyAction> preyActions = null;
	protected HashMap<String, String> config = new HashMap<String, String>();

	private static HashMap<String, PreyAction> getAvailablesActions() {
		if (preyActions == null) {
			HashMap<String, PreyAction> actions = new HashMap<String, PreyAction>();
			actions.put(LocationNotifierAction.DATA_ID, new LocationNotifierAction());
			actions.put(CallLogsNotifierAction.DATA_ID, new CallLogsNotifierAction());
			actions.put(RingtonePlayerAction.DATA_ID, new RingtonePlayerAction());
			actions.put(Mp3PlayerAction.DATA_ID, new Mp3PlayerAction());
			actions.put(PopUpAlertAction.DATA_ID, new PopUpAlertAction());
			actions.put(LockAction.DATA_ID, new LockAction());
			// Register here new available actions
			preyActions = actions;
		}
		return preyActions;
	}

	public static ArrayList<PreyAction> getActionsFromPreyControlStatus(ReportActionResponse preyControlStatus) {
		ArrayList<PreyAction> actions = new ArrayList<PreyAction>();
		for (Iterator<Map.Entry<String, PreyAction>> it = preyControlStatus.getActionsToPerform().entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, PreyAction> entry = it.next();
			actions.add(entry.getValue());
		}
		return actions;
	}

	public abstract String textToNotifyUserOnEachReport(Context ctx);

	/**
	 * Not every action should notify to user once executed. ie: Pop-alert
	 * 
	 * @return if Prey should visually notify when an action is executed.
	 */
	public abstract boolean shouldNotify();

	public abstract void execute(ActionJob actionJob, Context ctx) throws PreyException;

	public static PreyAction getActionFromName(String moduleName) {
		return getAvailablesActions().get(moduleName);
	}

	public HashMap<String, String> getConfig() {
		return config;
	}

	/**
	 * Represents the action's result behavior.
	 * 
	 * @return true when we have to expect a result. e.g. Data to be sent as
	 *         part of a report. <code>false</code> when this action behaves
	 *         asynchronous and can finish without return anything. e.g. Play a
	 *         ringtone.
	 */
	public abstract boolean isSyncAction();

	@Override
	public boolean equals(Object o) {
		return (this.ID).equals(((PreyAction)o).ID);
	}

	public void killAnyInstanceRunning(Context ctx) {
		
		
	}

}
