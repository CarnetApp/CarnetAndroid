package com.spisoft.quicknote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.spisoft.quicknote.browser.BrowserFragment;
import com.spisoft.quicknote.browser.PasteDialog;
import com.spisoft.quicknote.browser.PermissionChecker;
import com.spisoft.quicknote.browser.RecentNoteListFragment;
import com.spisoft.quicknote.browser.SearchFragment;
import com.spisoft.quicknote.databases.DBMergerService;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.editor.BlankFragment;
import com.spisoft.quicknote.synchro.HelpActivity;
import com.spisoft.quicknote.synchro.SynchroService;
import com.spisoft.quicknote.utils.FileLocker;
import com.spisoft.quicknote.utils.PinView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener, PinView.PasswordListener, NoteManager.UpdaterListener {

    private static final String WAS_LOCKED = "was_locked";
    private Fragment fragment;
    private View mRecentButton;
    private View mBrowserButton;
    private Toolbar mToolbar;
    private View mSettingsButton;
    private FrameLayout mLockLayout;
    private boolean isLocked;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private PermissionChecker mPermissionChecker;
    private Handler mHandler = new Handler();
    private boolean lockOnStart;
    private PasteDialog mPasteDialog;
    private boolean mIsPasteDialogDisplayed = false;
    private BroadcastReceiver mReceiver;
    private BlankFragment mEditorFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(HelpActivity.shouldStartActivity(this))
            startActivity(new Intent(this, HelpActivity.class));
        if(PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceHelper.NOTE_VERSION_PREF, -1)==-1&& PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceHelper.DISPLAY_AD_COUNT, 0)>0){
            //not set but already start  : V1
            PreferenceHelper.setCurrentNoteVersion(getApplicationContext(),1);
        }

        //startService(new Intent(this, SynchroService.class));
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
        if(!DBMergerService.isJobScheduledOrRunning(this)){
            DBMergerService.scheduleJob(this);
        }
        mPermissionChecker = new PermissionChecker();

        PreferenceHelper.incrementDisplayAd(this);
        setContentView(R.layout.activity_main);
        mLockLayout = (FrameLayout)findViewById(R.id.lock_layout);
        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mBrowserButton = findViewById(R.id.browser_button);
        mBrowserButton.setOnClickListener(this);
        mRecentButton = findViewById(R.id.recent_button);
        mRecentButton.setOnClickListener(this);
        mSettingsButton = findViewById(R.id.settings_button);
        mSettingsButton.setOnClickListener(this);
        mPermissionChecker.checkAndRequestPermission(this);
       // startService(new Intent(this, FloatingService.class));
        lockOnStart= true;
        if(savedInstanceState==null) {
            Fragment fragment = RecentNoteListFragment.newInstance();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    NoteManager.update(MainActivity.this, MainActivity.this);
                }
            }, 3000);

            setFragment(fragment);
            //setPreloadBlankFragment();

        }
        else
            lockOnStart = savedInstanceState.getBoolean(WAS_LOCKED,false);

        //lock when starting and when was locked before rotation
        if (PreferenceHelper.shouldLockOnMinimize(this)) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
            if(lockOnStart)
                lock();
        }

        mReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(FileManagerService.ACTION_COPY_ENDS)){
                   if(mIsPasteDialogDisplayed&&mPasteDialog!=null) {
                       mPasteDialog.dismiss();

                   }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(FileManagerService.ACTION_COPY_ENDS);

        registerReceiver(mReceiver, filter);
        if(FileManagerService.sIsCopying)
            displayPasteDialog();


        new Thread(){
            public void run(){
                Log.d("lockdebug","asking lock ");
                synchronized (FileLocker.getLockOnPath("/sdcard")){
                    Log.d("lockdebug","asking lock1 acquired");
                    try {
                        synchronized (FileLocker.getLockOnPath("/sdcard")){
                            Log.d("lockdebug","asking lock12 acquired");
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.d("lockdebug","asking lock12 released");
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("lockdebug","asking lock1 released");
                }
            }
        }.start();
        new Thread(){
            public void run(){
                Log.d("lockdebug","asking lock 2");
                synchronized (FileLocker.getLockOnPath("/sdcardbla")){
                    Log.d("lockdebug","asking lock2 acquired");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("lockdebug","asking lock2 released");
                }
            }
        }.start();
    }

    public void lockDrawer(){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void unlockDrawer(){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        mPermissionChecker.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    public void setTitle(int str){
        mToolbar.setTitle(str);
    }

    protected void onStop(){
        super.onStop();
        if(FloatingService.sService!=null)
            FloatingService.sService.requestMinimize();
    }


    protected void onDestroy(){
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(WAS_LOCKED,isLocked);

    }
    protected void onResume(){
        super.onResume();
        if(!isChangingConfigurations()) {
            if (!PreferenceHelper.shouldLockOnMinimize(this)) {
                if (isLocked)
                    onPasswordOk();
            }
        }


    }
    protected void  onPause(){
        super.onPause();
        if(PreferenceHelper.shouldLockOnMinimize(this)&&!isChangingConfigurations())
            lock();
    }

    private void lock() {
        if(isLocked)
            return;
        isLocked = true;
        PinView pinView = new PinView(this);
        pinView.setPasswordListener(this);
        mLockLayout.removeAllViews();
        mLockLayout.setVisibility(View.VISIBLE);
        mLockLayout.addView(pinView);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchView view = (SearchView) menu.findItem(R.id.action_search).getActionView();
        view.setOnQueryTextListener(this);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setFragment(Fragment fragment) {
        if(fragment instanceof BlankFragment){
            if(mEditorFrag!=null) {

                mEditorFrag.setArguments(fragment.getArguments());
                fragment = mEditorFrag;

            }
            //mEditorFrag = (BlankFragment) fragment;

        }
        this.fragment = fragment;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.root,fragment)
                .addToBackStack(null).commit();
    }

    public void setPreloadBlankFragment() {

            if(mEditorFrag==null) {
                mEditorFrag = BlankFragment.newInstance(NoteManager.createNewNote("test"));
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.blanfragment,mEditorFrag)
                        .commit();
            }




    }

    public void onBackPressed() {
        if(fragment instanceof com.spisoft.quicknote.editor.BlankFragment)
            if(((BlankFragment) fragment).onBackPressed())
                return;
        getSupportFragmentManager().popBackStackImmediate();

        if (getSupportFragmentManager().getBackStackEntryCount() == 0){
            finish();
            return;
        }
        this.fragment  = getSupportFragmentManager().getFragments().get(0);

    }

    @Override
    public void onClick(View view) {

        if(view==mRecentButton){
            clearBackstack();
            //getSupportFragmentManager().back
            Fragment fragment = RecentNoteListFragment.newInstance();
            setFragment(fragment);
            mDrawerLayout.closeDrawers();
        }else if(view==mBrowserButton){
            clearBackstack();
            Fragment fragment = BrowserFragment.newInstance(PreferenceHelper.getRootPath(this));
            setFragment(fragment);
            mDrawerLayout.closeDrawers();
        }
        else if(mSettingsButton==view){
            startActivity(new Intent(this, SettingsActivity.class));
            mDrawerLayout.closeDrawers();
        }
    }

    private void clearBackstack() {

        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if(fragment instanceof SearchFragment)
            ((SearchFragment) fragment).doSearch(query);
        else {
            Fragment fragment = SearchFragment.newInstance(PreferenceHelper.getRootPath(this),query);
            setFragment(fragment);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean checkPassword(String password) {
        if(PreferenceHelper.getPassword(this).equals(password))
            return true;
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onPasswordOk() {
        mLockLayout.removeAllViews();
        mLockLayout.setVisibility(View.GONE);
        isLocked = false;
    }

    @Override
    public void onUpdateFileList(int length) {

    }

    @Override
    public void onUpdate(String name) {
        Log.d("udpatedebug", "updating " + name);
    }

    @Override
    public void onUpdateError() {

    }

    @Override
    public void onUpdateFinished() {

    }

    @Override
    public void onUpdateStart() {

    }

    public void displayPasteDialog() {
        if(!mIsPasteDialogDisplayed){
            mPasteDialog = new PasteDialog(this);
            mPasteDialog.show();
            mIsPasteDialogDisplayed = true;
            mPasteDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    mIsPasteDialogDisplayed=false;
                }
            });

        }
    }
}
