package com.prey.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.prey.R;

public class DelayedActivatedEditTextPreference extends EditTextPreference {

	protected final int CLICKS_TO_ENABLE = 7;

	protected OnPreferenceActivateListener mActivateListener = null;
	protected boolean mEnabledAppearance = false;
	protected int mPrefEnableCounter = 0;
	
	protected Toast mCurrentToast = null;
	
	protected Resources res = null;
	protected String mHelpMsg = null;
	protected int mHelpMsgPluralResId = -1;
	
	protected String mActivateMsg = null;
	protected String mAlreadyActiveMsg = null;
	protected int mHelpMsgThreshold = -1;
	protected int mActivateThreshold = 7;

	public DelayedActivatedEditTextPreference(Context context) {
		this(context, null);
	}
	
	public DelayedActivatedEditTextPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public DelayedActivatedEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.DelayedActivatedEditTextPref);
			
			res = a.getResources();
			
	        for (int i = 0, N = a.getIndexCount(); i < N; ++i) {
	            int attr = a.getIndex(i);
	            
				switch (attr) {
				case R.styleable.DelayedActivatedEditTextPref_hiddenClickActivateMsg:
					mActivateMsg = a.getString(attr);
					break;
				case R.styleable.DelayedActivatedEditTextPref_hiddenClickAlreadyActiveMsg:
					mAlreadyActiveMsg = a.getString(attr);
					break;
				case R.styleable.DelayedActivatedEditTextPref_hiddenClickHelpMsg:
					int rId = a.getResourceId(attr, -1);
					if (rId != -1 && "plurals".equals(res.getResourceTypeName(rId))) {
						mHelpMsgPluralResId = rId;
					} else {
						mHelpMsg = a.getString(attr);
					}
					break;
				case R.styleable.DelayedActivatedEditTextPref_hiddenClickActivateThreshold:
					mActivateThreshold = a.getInteger(attr, mActivateThreshold);
					break;
				case R.styleable.DelayedActivatedEditTextPref_hiddenClickHelpMsgThreshold:
					mHelpMsgThreshold = a.getInteger(attr, mHelpMsgThreshold);
					break;
				}
	        }
			
	        a.recycle();
		}
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		boolean viewEnabled = isEnabled() && mEnabledAppearance;
		enableView(view, viewEnabled);
	}

	protected void enableView(View view, boolean enabled) {
		view.setEnabled(enabled);
		if (view instanceof ViewGroup) {
			ViewGroup grp = (ViewGroup) view;
			for (int index = 0; index < grp.getChildCount(); index++)
				enableView(grp.getChildAt(index), enabled);
		}
	}
	
	public void setCounter(int value) {
		// prevent undesired effects
		if (value > mActivateThreshold)
			value = mActivateThreshold;
		
		mPrefEnableCounter = value;
	}

	public void setEnabledAppearance(boolean enabled) {
		mEnabledAppearance = enabled;
		notifyChanged();
	}
	
	protected void onActivate() {
		if (mActivateListener != null) {
			if (mActivateListener.onPreferenceActivate(this))
				setEnabledAppearance(true);
		}
	}
	
	public void setOnPreferenceActivateListener(OnPreferenceActivateListener preferenceActivateListener) {
		mActivateListener = preferenceActivateListener;
	}
	
	protected String formatHelpMessage(int clicksLeft) {
		if (mHelpMsgPluralResId != -1) {
			return res.getQuantityString(mHelpMsgPluralResId, clicksLeft, clicksLeft);
		} else if (mHelpMsg != null) {
			return String.format(mHelpMsg, clicksLeft);
		}
		
		return null;
	}
	
	protected void showActiveMessage(boolean first) {
		// show message based on what's available
		String s = null;
		if (mPrefEnableCounter > mActivateThreshold && mAlreadyActiveMsg != null) {
			s = mAlreadyActiveMsg;
		}
		else if (mActivateMsg != null) {					
			s = mActivateMsg;
		}
		else {
			s = formatHelpMessage(0);
		}
		
		if (s != null) {
			mCurrentToast = Toast.makeText(getContext(), s, Toast.LENGTH_LONG);
			mCurrentToast.show();
		}
	}
	
	public void showActiveMessage() {
		showActiveMessage(false);
	}

	@Override
	protected void onClick() {
		if (mEnabledAppearance)
			super.onClick();
		else {
			++mPrefEnableCounter;
			
			// do not wait for toast to disappear
			if (mCurrentToast != null) {
				mCurrentToast.cancel();
				mCurrentToast = null;
			}
			
			if (mPrefEnableCounter >= mActivateThreshold) {
				onActivate();
				showActiveMessage(mPrefEnableCounter > mActivateThreshold
						&& mAlreadyActiveMsg != null);
				
				// do not overflow
				mPrefEnableCounter = mActivateThreshold;
			}
			else if (mHelpMsgThreshold > 0 && mPrefEnableCounter >= mHelpMsgThreshold) {
				String s = formatHelpMessage(mActivateThreshold - mPrefEnableCounter);
				if (s != null) {
					mCurrentToast = Toast.makeText(getContext(), s, Toast.LENGTH_LONG);
					mCurrentToast.show();
				}
			}
		}
	}
	
	public interface OnPreferenceActivateListener {
		public abstract boolean onPreferenceActivate(Preference preference);
	}
}
