/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.exceptions.NoMoreDevicesAllowedException;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

import java.util.logging.Logger;

public class InitActivity extends FragmentActivity {


    ViewPager mViewPager;
    OnboardingPagerAdapter onboardingPagerAdapter;

    @Override
    public void onResume() {
        PreyLogger.d("onResume of InitActivity");
        super.onResume();

    }

    @Override
    public void onPause() {
        PreyLogger.d("onPause of InitActivity");
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setContentView(R.layout.home);
        PreyLogger.d("onCreate of InitActivity");

        onboardingPagerAdapter = new OnboardingPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(onboardingPagerAdapter);

        Button buttonHome = (Button) findViewById(R.id.buttonHome);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                finish();


            }
        });
        TextView textView = (TextView) findViewById(R.id.linkHome);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");
        buttonHome.setTypeface(titilliumWebBold);


        final LinearLayout larr=(LinearLayout)findViewById(R.id.larr);
        final LinearLayout rarr=(LinearLayout)findViewById(R.id.rarr);
        larr.setVisibility(View.GONE);
        larr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int currentItem = mViewPager.getCurrentItem();
                larr.setVisibility(View.VISIBLE);
                rarr.setVisibility(View.VISIBLE);
                if (currentItem == 0) {
                    larr.setVisibility(View.GONE);
                }

                mViewPager.setCurrentItem(currentItem - 1);
            }
        });

        rarr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int currentItem = mViewPager.getCurrentItem();
                larr.setVisibility(View.VISIBLE);
                rarr.setVisibility(View.VISIBLE);
                if (currentItem >= 5) {
                    rarr.setVisibility(View.GONE);
                    onboardingCompleted();
                }

                mViewPager.setCurrentItem(currentItem + 1);
            }
        });

        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int currentItem = mViewPager.getCurrentItem();
                larr.setVisibility(View.VISIBLE);
                rarr.setVisibility(View.VISIBLE);
                PreyLogger.d("currentItem:" + currentItem);
                if (currentItem == 0) {
                    larr.setVisibility(View.GONE);
                }
                if (currentItem  >=5) {
                    rarr.setVisibility(View.GONE);
                    onboardingCompleted();
                }

                return false;
            }
        });


        onboardingInit();

    }

    public void onboardingInit(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new OnboardingInitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new OnboardingInitTask().execute();
    }

    public void onboardingCompleted(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new OnboardingCompletedTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new OnboardingCompletedTask().execute();
    }

    public void movePage(int page) {
        mViewPager.setCurrentItem(page);
    }


    public static class OnboardingPagerAdapter extends FragmentStatePagerAdapter {

        public OnboardingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public int page = 0;

        @Override
        public Fragment getItem(int i) {

            page = i;
            Fragment fragment = null;
            if (i == 0) {
                fragment = new DemoObjectFragment0();


            }
            if (i == 1) {
                fragment = new DemoObjectFragment1();
            }
            if (i == 2) {
                fragment = new DemoObjectFragment2();
            }
            if (i == 3) {
                fragment = new DemoObjectFragment3();
            }
            if (i == 4) {
                fragment = new DemoObjectFragment4();
            }
            if (i == 5) {
                fragment = new DemoObjectFragment5();
            }
            if (i == 6) {
                fragment = new DemoObjectFragment6();
            }


            return fragment;
        }

        @Override
        public int getCount() {

            return 7;
        }
    }


    public static class DemoObjectFragment0 extends Fragment {


        public ViewPager mViewPager;

        public void setViewPager(ViewPager mViewPager) {
            this.mViewPager = mViewPager;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home0, container, false);
            try {
                Typeface titilliumWebRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
                TextView textView1 = (TextView) rootView.findViewById(R.id.textView1);
                textView1.setTypeface(titilliumWebRegular);
            }catch (Exception e){}
            return rootView;
        }
    }


    public static class DemoObjectFragment1 extends Fragment {


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home1, container, false);
            try {
                Typeface titilliumWebRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
                TextView textView1 = (TextView) rootView.findViewById(R.id.textView1);
                textView1.setTypeface(titilliumWebRegular);
            }catch (Exception e){}
            return rootView;
        }
    }

    public static class DemoObjectFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home2, container, false);
            try {
                Typeface titilliumWebRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
                TextView textView1 = (TextView) rootView.findViewById(R.id.textView1);
                textView1.setTypeface(titilliumWebRegular);
            }catch (Exception e){}
            return rootView;
        }
    }


    public static class DemoObjectFragment3 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home3, container, false);
            try {
                Typeface titilliumWebRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
                TextView textView1 = (TextView) rootView.findViewById(R.id.textView1);
                textView1.setTypeface(titilliumWebRegular);
            }catch (Exception e){}
            return rootView;
        }
    }


    public static class DemoObjectFragment4 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home4, container, false);
            try {
                Typeface titilliumWebRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
                TextView textView1 = (TextView) rootView.findViewById(R.id.textView1);
                textView1.setTypeface(titilliumWebRegular);
            }catch (Exception e){}
            return rootView;
        }
    }

    public static class DemoObjectFragment5 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home5, container, false);
            try {
                Typeface titilliumWebRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
                TextView textView1 = (TextView) rootView.findViewById(R.id.textView1);
                textView1.setTypeface(titilliumWebRegular);
            }catch (Exception e){}
            return rootView;
        }
    }

    public static class DemoObjectFragment6 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home6, container, false);
            try {
                Typeface titilliumWebRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
                TextView textView1 = (TextView) rootView.findViewById(R.id.textView1);
                textView1.setTypeface(titilliumWebRegular);
            }catch (Exception e){}
            return rootView;
        }
    }



    private class OnboardingInitTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... data) {
            PreyWebServices.getInstance().sendEvent(getApplication(), PreyConfig.ANDROID_ONBOARDING_INIT);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {

        }

    }

    private class OnboardingCompletedTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... data) {
            PreyWebServices.getInstance().sendEvent(getApplication(), PreyConfig.ANDROID_ONBOARDING_COMPLETED);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {

        }

    }
}
