package com.prey.actions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.prey.actions.observer.ActionJob;
import com.prey.actions.observer.ActionResult;
import com.prey.activities.ScreenShotActivity;
import com.prey.exceptions.PreyException;

public class ScreenshotsAction  extends PreyAction {

	public static final String DATA_ID = "screenshot";
	public static ActionJob actionJob;
	
	@Override
	public String textToNotifyUserOnEachReport(Context ctx) {
		return "";
	}

	@Override
	public boolean shouldNotify() {
		return true;
	}

	@Override
	public void execute(ActionJob actionJob2, Context ctx) throws PreyException {

		actionJob = actionJob2;
		Bundle bundle = new Bundle();
		
		
		Intent popup = new Intent(ctx, ScreenShotActivity.class);
		popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		popup.putExtras(bundle);
		ctx.startActivity(popup);
		
		ActionResult result = new ActionResult();
		//result.setDataToSend(data);
		actionJob.finish(result);
		
	}

	@Override
	public boolean isSyncAction() {
		return true;
	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

 

}
