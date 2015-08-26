package com.prey.activities.frames;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prey.PreyLogger;
import com.prey.R;
import com.prey.activities.WelcomeActivity;

public class PrivilegesOkFrame extends Fragment {
		
	private WelcomeActivity welcome;
	
	public void setActivity(WelcomeActivity welcome) {
		this.welcome = welcome;
	}

	@Override
	  public void onResume() {
	     PreyLogger.i("onResume of PrivilegesOkFrame");
	     super.onResume();
	  }

	  @Override
	  public void onPause() {
		  PreyLogger.i("OnPause of PrivilegesOkFrame");
	    super.onPause();
	  }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // Defines the xml file for the fragment
      View view = inflater.inflate(R.layout.privileges, container, false);
      return view;
    }

}
