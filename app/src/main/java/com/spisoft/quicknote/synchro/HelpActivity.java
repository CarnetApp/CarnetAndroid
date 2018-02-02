package com.spisoft.quicknote.synchro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.synchro.googledrive.AuthorizeActivity;
import com.spisoft.sync.account.*;
import com.spisoft.sync.wrappers.*;
import com.spisoft.sync.wrappers.nextcloud.NextCloudAuthorizeFragment;
import com.spisoft.sync.wrappers.nextcloud.NextCloudCredentialsHelper;
import com.spisoft.sync.wrappers.nextcloud.NextCloudWrapper;

public class HelpActivity extends AppCompatActivity implements NextCloudAuthorizeFragment.OnConnectClickListener {

    private static final String SHOULD_START_ACTIVITY = "should_start_gdrive_act";

    private  int NUM_PAGES = 2;

    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private NextCloudAuthorizeFragment mNextCloudFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(SHOULD_START_ACTIVITY, false).commit();
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

    }
    public void addAccount(View v){
        startActivity(new Intent(this,AuthorizeActivity.class));
        finish();
    }
    public void exit(View v){
        finish();
    }
    public static boolean shouldStartActivity(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SHOULD_START_ACTIVITY, true);
    }

    public void next() {
        mPager.setCurrentItem(mPager.getCurrentItem()+1);
    }

    @Override
    public void onConnectClick(String remote, String username, String password) {
        com.spisoft.sync.account.DBAccountHelper.Account account = com.spisoft.sync.account.DBAccountHelper.getInstance(this)
                .addOrReplaceAccount(new com.spisoft.sync.account.DBAccountHelper.Account(-1, NextCloudWrapper.ACCOUNT_TYPE, "NextCloud"));
        NextCloudCredentialsHelper.getInstance(this).addOrReplaceAccount(new NextCloudCredentialsHelper.Credentials(-1, account.accountID, remote, username, password));
        com.spisoft.sync.wrappers.WrapperFactory.getWrapper(this,NextCloudWrapper.ACCOUNT_TYPE, account.accountID).addFolderSync(PreferenceHelper.getRootPath(this), "Documents/QuickNote");
        finish();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0)
                return new SynchroIntroductionFragment();
            else {
                mNextCloudFragment = new NextCloudAuthorizeFragment();
                mNextCloudFragment.setOnConnectClickListener(HelpActivity.this);
                return mNextCloudFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}

