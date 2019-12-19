package com.spisoft.quicknote.intro;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.sync.wrappers.Wrapper;
import com.spisoft.sync.wrappers.WrapperFactory;
import com.spisoft.sync.wrappers.nextcloud.NextCloudAuthorizeFragment;
import com.spisoft.sync.wrappers.nextcloud.NextCloudCredentialsHelper;
import com.spisoft.sync.wrappers.nextcloud.NextCloudWrapper;

public class HelpActivity extends AppCompatActivity implements NextCloudAuthorizeFragment.OnConnectedListener {

    public static final String SYNC_ONLY = "sync_only";
    private static final String SHOULD_START_ACTIVITY = "should_start_gdrive_act";

    private  int NUM_PAGES = 6;
    public String TAG = "HelpActivity";
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private NextCloudAuthorizeFragment mNextCloudFragment;
    private Wrapper mDriveWrapper;
    private String mInstance;
    private boolean mSyncOnly;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        mSyncOnly = getIntent().getBooleanExtra(SYNC_ONLY, false);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(SHOULD_START_ACTIVITY, false).commit();
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

    }

    public void exit(View v){
        finish();
    }
    public static boolean shouldStartActivity(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SHOULD_START_ACTIVITY, true)||true;
    }

    public void next() {
        if(mPager.getCurrentItem() < mPagerAdapter.getCount()-1)
            mPager.setCurrentItem(mPager.getCurrentItem()+1);
        else
            finish();
    }

    @Override
    public void onConnected(boolean isSSoLogin, String remote, String username, String password) {
        com.spisoft.sync.account.DBAccountHelper.Account account = com.spisoft.sync.account.DBAccountHelper.getInstance(this)
                .addOrReplaceAccount(new com.spisoft.sync.account.DBAccountHelper.Account(-1, NextCloudWrapper.ACCOUNT_TYPE, "NextCloud"));
        NextCloudCredentialsHelper.getInstance(this).addOrReplaceAccount(new NextCloudCredentialsHelper.Credentials(-1, account.accountID, remote, username, password));
        com.spisoft.sync.wrappers.WrapperFactory.getWrapper(this,NextCloudWrapper.ACCOUNT_TYPE, account.accountID).addFolderSync(PreferenceHelper.getRootPath(this), "Documents/QuickNote");
        next();
    }

    public void connectGoogleDrive() {
        Log.d("wrapperdebug","connectGoogleDrive");

        for(Wrapper wrapper : WrapperFactory.getWrapperList(this)){
            Log.d("wrapperdebug","name "+wrapper.getClass().getSimpleName());
            if(wrapper.getClass().getSimpleName().equals("GDriveWrapper")){
                mDriveWrapper = wrapper;
                wrapper.startAuthorizeActivityForResult(this,0);
            }
        }
        if(mDriveWrapper == null){
            Toast.makeText(this, R.string.not_available_on_fdroid, Toast.LENGTH_SHORT).show();
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(mSyncOnly)
                position = position + 2;
            if(position == 0)
                return new WelcomeIntroductionFragment();
            else if (position == 1){
                return new SayHiFragment();
            }
            else if(position == 2)
                return new ChooseInstanceFragment();
            else if(position == 3)
                return new SynchroIntroductionFragment();
            else  if(position == 4){
                mNextCloudFragment = new NextCloudAuthorizeFragment();
                mNextCloudFragment.setOnConnectClickListener(HelpActivity.this);
                if(mInstance != null)
                    mNextCloudFragment.setInstance(mInstance);
                mInstance = null;
                return mNextCloudFragment;
            } else {
                return new DisableOptimizationFragment();

            }
        }

        @Override
        public int getCount() {
	        int count = mSyncOnly?NUM_PAGES-2:NUM_PAGES;
            if(Build.VERSION.SDK_INT< Build.VERSION_CODES.M)
                count--;
            return count;
        }
    }

    public void goToNextcloudFrag(String instance){
        mPager.setCurrentItem(mSyncOnly?2:4);
        if(mNextCloudFragment == null)
            mInstance = instance;
        else
            mNextCloudFragment.setInstance(instance);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d(TAG, requestCode+" onActivityResult "+resultCode);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    onGoogleConnectionOK();
                    }

                break;
        }
        super.onActivityResult(requestCode,resultCode, data);

    }
    public void skip(View v){
        finish();
    }
    public void next(View v){
        next();
    }

    private void onGoogleConnectionOK() {
        com.spisoft.sync.account.DBAccountHelper.Account account = com.spisoft.sync.account.DBAccountHelper.getInstance(this)
                .addOrReplaceAccount(new com.spisoft.sync.account.DBAccountHelper.Account(-1,mDriveWrapper.getAccountType(), "Google Drive"));
        com.spisoft.sync.wrappers.WrapperFactory.getWrapper(this,mDriveWrapper.getAccountType(), account.accountID).addFolderSync(PreferenceHelper.getRootPath(this), "Documents/QuickNote");

        finish();

    }
}

