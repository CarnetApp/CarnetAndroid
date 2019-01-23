package com.spisoft.quicknote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.spisoft.quicknote.browser.PasteDialog;
import com.spisoft.quicknote.browser.PermissionChecker;
import com.spisoft.quicknote.databases.DBMergerService;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.editor.BlankFragment;
import com.spisoft.quicknote.editor.EditorView;
import com.spisoft.quicknote.intro.HelpActivity;
import com.spisoft.quicknote.updater.UpdaterActivity;
import com.spisoft.quicknote.utils.PinView;
import com.spisoft.sync.Configuration;
import com.spisoft.sync.account.DBAccountHelper;
import com.spisoft.sync.synchro.SynchroService;

public class MainActivity extends AppCompatActivity implements PinView.PasswordListener, NoteManager.UpdaterListener, Configuration.SyncStatusListener {
    public static final String ACTION_RELOAD_KEYWORDS = "action_reload_keywords";

    private static final String WAS_LOCKED = "was_locked";
    private static final int UPDATE_REQUEST_CODE = 100;
    private Fragment fragment;
    private FrameLayout mLockLayout;
    private boolean isLocked;
    private PermissionChecker mPermissionChecker;
    private Handler mHandler = new Handler();
    private boolean lockOnStart;
    private PasteDialog mPasteDialog;
    private boolean mIsPasteDialogDisplayed = false;
    private BroadcastReceiver mReceiver;
    private BlankFragment mEditorFrag;
    private boolean mShouldRemove;
    private Fragment mFragmentToPut;
    private Bundle mSavedInstanceState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String theme = PreferenceManager.getDefaultSharedPreferences(this).getString("theme","carnet");
        switch(theme){
            case "dark":
                setTheme(R.style.CarnetTheme_Dark);
                break;

        }
        mSavedInstanceState = savedInstanceState;
        if(PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceHelper.NOTE_VERSION_PREF, -1)==-1){
            //not set but already start  : V1
            PreferenceHelper.setCurrentNoteVersion(getApplicationContext(),1);
        }
        setContentView(R.layout.activity_main);
        mLockLayout = (FrameLayout)findViewById(R.id.lock_layout);
        if(!UpdaterActivity.startUpdateIfNeeded(this, UPDATE_REQUEST_CODE)){
            onUpdateDone();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.add(0, R.string.sync, 1, R.string.sync);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.string.sync);
        item.setEnabled(!SynchroService.isSyncing);
        if(SynchroService.isSyncing){
            item.setIcon(R.drawable.sync_dim);
        } else {
            TypedArray a = getTheme().obtainStyledAttributes(new int[] {R.attr.SyncIcon});
            int attributeResourceId = a.getResourceId(0, 0);
            Drawable drawable = getResources().getDrawable(attributeResourceId);
            item.setIcon(drawable);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.string.sync){
            Cursor cursor = DBAccountHelper.getInstance(this).getCursor();
            if(cursor == null || cursor.getCount() == 0){
                startActivity(new Intent(this, HelpActivity.class));
            }
            else {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("refuse_certificate", false).apply();
                startService(new Intent(this, SynchroService.class));
            }
            return true;
        }
        return false;
    }

    @Override
    public void onSyncStatusChanged(boolean isSyncing) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();            }
        });
    }

    private void onUpdateDone() {



        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            if(!DBMergerService.isJobScheduledOrRunning(this)){
                DBMergerService.scheduleJob(this,true, DBMergerService.ALL_DATABASES);
            }
        int count = PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceHelper.LAUNCH_COUNT, 1);
        if(count%30 == 0 && !PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(PreferenceHelper.HAS_DONATE, false)){
            Snackbar.make(findViewById(R.id.root), R.string.donation_ask,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.donate, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openDonation(null);
                        }
                    })
                    .show();
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(PreferenceHelper.LAUNCH_COUNT, count+1).commit();
        mPermissionChecker = new PermissionChecker();
        mPermissionChecker.checkAndRequestPermission(this);

        // startService(new Intent(this, FloatingService.class));
        lockOnStart= true;
        if(mSavedInstanceState==null) {
            Fragment fragment = MainFragment.newInstance();

            mFragmentToPut = fragment;
            setFragment(mFragmentToPut);
            //setFragment(fragment);
            if(HelpActivity.shouldStartActivity(this))
                startActivity(new Intent(this, HelpActivity.class));

        }
        else
            lockOnStart = mSavedInstanceState.getBoolean(WAS_LOCKED,false);

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
        mSavedInstanceState = null;
    }

    public void openDonation(View view) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(PreferenceHelper.HAS_DONATE, true).commit();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=YMHT55NSCLER6"));
        startActivity(browserIntent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(!(EditorView.sEditorView != null && EditorView.sEditorView.onRequestPermissionsResult(requestCode, permissions, grantResults)))
            mPermissionChecker.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    public void setTitle(int str){
        if(getSupportActionBar()!=null)
            getSupportActionBar().setTitle(str);
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
        startService(new Intent(this, SynchroService.class));
        if(!isChangingConfigurations()) {
            if (!PreferenceHelper.shouldLockOnMinimize(this)) {
                if (isLocked)
                    onPasswordOk();
            }
        }
        Configuration.addSyncStatusListener(this);
    }
    protected void  onPause(){
        super.onPause();
        if(PreferenceHelper.shouldLockOnMinimize(this)&&!isChangingConfigurations())
            lock();
        Configuration.removeSyncStatusListener(this);
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





    public void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in,
                R.anim.fade_out);
        if(fragment instanceof BlankFragment){
            if(mEditorFrag!=null) {

                mEditorFrag.setArguments(fragment.getArguments());
                fragment = mEditorFrag;

            }
            mEditorFrag = (BlankFragment) fragment;
        }
        transaction.replace(R.id.root,fragment);

        transaction.addToBackStack(fragment.getClass().getName()).commit();
        this.fragment = fragment;

    }
    @Override
    public void onAttachFragment(Fragment fragment){
        super.onAttachFragment(fragment);
        if(mShouldRemove && fragment == mEditorFrag) {
            getSupportFragmentManager().popBackStack();
            mShouldRemove = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UPDATE_REQUEST_CODE){
            onUpdateDone();
        }
        else if(EditorView.sEditorView!=null)
            EditorView.sEditorView.onActivityResult(requestCode, resultCode, data);
    }
    public void setPreloadBlankFragment() {

            if(mEditorFrag==null) {
                mShouldRemove = true;
                mEditorFrag = BlankFragment.newInstance(null);
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(mEditorFrag.getClass().getName())
                        .replace(R.id.root,mEditorFrag)
                        .commit();
            }




    }

    public void onBackPressed() {
        if(fragment instanceof com.spisoft.quicknote.editor.BlankFragment)
            if(((com.spisoft.quicknote.editor.BlankFragment) fragment).onBackPressed())
                return;
        if(fragment instanceof com.spisoft.quicknote.MainFragment)
            if(((com.spisoft.quicknote.MainFragment) fragment).onBackPressed())
                return;
        if (getSupportFragmentManager().getBackStackEntryCount() == 1){
            finish();
            return;
        }

        getSupportFragmentManager().popBackStackImmediate();
        this.fragment  = getSupportFragmentManager().getFragments().get(0);
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

    public void superOnBackPressed() {
        getSupportFragmentManager().popBackStackImmediate();
        this.fragment  = getSupportFragmentManager().getFragments().get(0);
    }
}
