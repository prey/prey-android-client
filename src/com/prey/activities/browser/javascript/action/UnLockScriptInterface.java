package com.prey.activities.browser.javascript.action;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;

public class UnLockScriptInterface {

	private EditText userInput = null;

	private Context ctx = null;

	public UnLockScriptInterface(Context ctx) {
		this.ctx = ctx;
	}
	
	public void execute(  boolean unlockPass) {
/*
		if (unlockPass) {
			PreyLogger.i("unlockPass:" + unlockPass);

			InputFilter[] filter = new InputFilter[1];
			filter[0] = new InputFilter.LengthFilter(4);
			// input.setFilters(filter);
			PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);

			LayoutInflater li = LayoutInflater.from(ctx);
			View promptsView = li.inflate(R.layout.prompts_pin, null);

			userInput = (EditText) promptsView.findViewById(R.id.editTextPin);
			userInput.setFilters(filter);
			userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
			userInput.setText(preyConfig.getDigitUninstallPin());

			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setMessage(R.string.preferences_admin_device_revoked_password_dialog_title);
			builder.setView(promptsView);

			builder.setPositiveButton(R.string.ok, new SecurityOkOnClickListener(ctx));
			builder.setNegativeButton(R.string.cancel, new SecurityCancelOnClickListener(ctx));

			builder.show();
		} else {
			Toast.makeText(ctx, "UnLock disable:", Toast.LENGTH_SHORT).show();
		}*/
	}

	public class SecurityOkOnClickListener implements OnClickListener {
		private Context ctx = null;

		public SecurityOkOnClickListener(Context ctx) {
			super();
			this.ctx = ctx;
		}

		public void onClick(DialogInterface dialog, int which) {
			PreyLogger.i("text:" + userInput.getText());
			//PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
			//preyConfig.setUnInstallPinDigit(true, userInput.getText().toString());
		}

	}

	public class SecurityCancelOnClickListener implements OnClickListener {
		private Context ctx = null;

		public SecurityCancelOnClickListener(Context ctx) {
			super();
			this.ctx = ctx;
		}

		public void onClick(DialogInterface dialog, int which) {
			PreyLogger.i("text:" + userInput.getText());
			//PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
			//preyConfig.setUnInstallPinDigit(false, userInput.getText().toString());

		}

	}

}
