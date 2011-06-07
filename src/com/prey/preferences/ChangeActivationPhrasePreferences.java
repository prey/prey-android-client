package com.prey.preferences;

import com.prey.PreyLogger;
import com.prey.net.PreyWebServices;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class ChangeActivationPhrasePreferences extends EditTextPreference {
	
	
	Context ctx = null;
	public ChangeActivationPhrasePreferences(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.ctx = context;
	}

	public ChangeActivationPhrasePreferences(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
	}

	public ChangeActivationPhrasePreferences(Context context) {
		super(context);
		this.ctx = context;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// TODO Auto-generated method stub
		super.onDialogClosed(positiveResult);
		if (positiveResult){
			PreyLogger.d("Activation phrase changed to:" + getText());
			PreyWebServices.getInstance().updateActivationPhrase(ctx, getText());
		}
	}

}
