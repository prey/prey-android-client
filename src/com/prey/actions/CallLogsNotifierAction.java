package com.prey.actions;

import android.content.Context;
import android.database.Cursor;

import com.prey.R;
import com.prey.actions.observer.ActionJob;
import com.prey.actions.observer.ActionResult;

public class CallLogsNotifierAction extends PreyAction {

	public static final String DATA_ID = "calls";
	public final String ID = "calls";

	@Override
	public void execute(ActionJob actionJob, Context ctx) {
		// Querying for a cursor is like querying for any SQL-Database
		Cursor c = ctx.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");

		// Retrieve the column-indixes of phoneNumber, date and calltype
		int numberColumn = c.getColumnIndex(android.provider.CallLog.Calls.NUMBER);
		int dateColumn = c.getColumnIndex(android.provider.CallLog.Calls.DATE);
		// type can be: Incoming, Outgoing or Missed
		int typeColumn = c.getColumnIndex(android.provider.CallLog.Calls.TYPE);

		// Will hold the calls, available to the cursor

		HttpDataService data = new HttpDataService(CallLogsNotifierAction.DATA_ID);
		data.setList(true);

		// Loop through all entries the cursor provides to us.
		// if(c.first()){
		if (c.moveToFirst()) {
			do {
				String callerPhoneNumber = c.getString(numberColumn);
				int callDate = c.getInt(dateColumn);
				int callType = c.getInt(typeColumn);
				String type = null;
				// Drawable currentIcon = null;
				switch (callType) {
				case android.provider.CallLog.Calls.INCOMING_TYPE:
					type = "RECVD";
					break;
				case android.provider.CallLog.Calls.MISSED_TYPE:
					type = "MISSED";
					break;
				case android.provider.CallLog.Calls.OUTGOING_TYPE:
					type = "CALLED";
					break;
				}
				// // Convert the unix-timestamp to a readable datestring
				// String dateString = DateUtils.formatDateTime(ctx, callDate,
				// DateUtils.FORMAT_24HOUR+DateUtils.FORMAT_SHOW_TIME+DateUtils.FORMAT_SHOW_DATE).toString();
				//
				data.getDataList().put(Integer.toString(callDate), callerPhoneNumber + " [" + type + "]");
			} while (c.moveToNext());
		}

		ActionResult result = new ActionResult();
		result.setDataToSend(data);
		actionJob.finish(result);
	}

	@Override
	public String textToNotifyUserOnEachReport(Context ctx) {
		String prefix = ctx.getText(R.string.call_logs_notification_prefix).toString();
		return prefix;
	}

	@Override
	public boolean isSyncAction() {
		return true;
	}

	@Override
	public boolean shouldNotify() {
		return false;
	}

}
