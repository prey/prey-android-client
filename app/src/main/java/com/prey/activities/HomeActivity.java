package com.prey.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.prey.PreyLogger;
import com.prey.R;

/**
 * Created by oso on 23-12-15.
 */
public class HomeActivity extends FragmentActivity {


    ViewPager mViewPager;
    OnboardingPagerAdapter onboardingPagerAdapter;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.home);

        onboardingPagerAdapter=new OnboardingPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(onboardingPagerAdapter);

        Button buttonHome=(Button)findViewById(R.id.buttonHome);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                finish();



            }
        });
        TextView textView=(TextView)findViewById(R.id.linkHome);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    private class MovePageTask extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... data) {
            PreyLogger.i("Esperar 10");
            try{Thread.sleep(10000);}catch (Exception e){}
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            int page=onboardingPagerAdapter.page;
            PreyLogger.i("page:" + page);
            if(page==6){
                page=0;
            }

            mViewPager.setCurrentItem(page);

        }

    }


    @Override
    public void onBackPressed(){

    }


    public static class OnboardingPagerAdapter extends FragmentStatePagerAdapter {

        public OnboardingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public int page=0;

        @Override
        public Fragment getItem(int i) {
            PreyLogger.i("getItem:" + i);
            page=i;
            Fragment fragment = null;
            if (i==0) {
                fragment = new DemoObjectFragment0();
            }
            if (i==1) {
                fragment = new DemoObjectFragment1();
            }
            if (i==2) {
                fragment = new DemoObjectFragment2();
            }
            if (i==3) {
                fragment = new DemoObjectFragment3();
            }
            if (i==4) {
                fragment = new DemoObjectFragment4();
            }
            if (i==5) {
                fragment = new DemoObjectFragment5();
            }
            if (i==6) {
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


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home0, container, false);

            return rootView;
        }
    }

    public static class DemoObjectFragment1 extends Fragment {


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home1, container, false);

            return rootView;
        }
    }

    public static class DemoObjectFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home2, container, false);

            return rootView;
        }
    }


    public static class DemoObjectFragment3 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home3, container, false);

            return rootView;
        }
    }


    public static class DemoObjectFragment4 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home4, container, false);

            return rootView;
        }
    }

    public static class DemoObjectFragment5 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home5, container, false);

            return rootView;
        }
    }

    public static class DemoObjectFragment6 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frame_home6, container, false);

            return rootView;
        }
    }

}
