package com.spisoft.quicknote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.spisoft.quicknote.browser.PasteDialog;
import com.spisoft.quicknote.browser.PermissionChecker;
import com.spisoft.quicknote.databases.CacheBuilderIntentService;
import com.spisoft.quicknote.databases.DBMergerService;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;
import com.spisoft.quicknote.reminders.RemindersManager;
import com.spisoft.quicknote.editor.BlankFragment;
import com.spisoft.quicknote.editor.EditorView;
import com.spisoft.quicknote.editor.EditorActivity;
import com.spisoft.quicknote.intro.HelpActivity;
import com.spisoft.quicknote.updater.UpdaterActivity;
import com.spisoft.quicknote.utils.PinView;
import com.spisoft.quicknote.utils.Utils;
import com.spisoft.sync.Configuration;
import com.spisoft.sync.account.AccountListActivity;
import com.spisoft.sync.account.DBAccountHelper;
import com.spisoft.sync.synchro.SynchroService;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PinView.PasswordListener, NoteManager.UpdaterListener, Configuration.SyncStatusListener, EditorActivity {
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
    public static final String ACTION_OPEN_NOTE = "open_note";
    public static final String PATH = "note_path";

    private boolean mIsUpdateDone;
    private Intent mNewIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String theme = PreferenceManager.getDefaultSharedPreferences(this).getString("theme","carnet");
        switch(theme){
            case "dark":
                setTheme(R.style.CarnetTheme_Dark);
                break;
            case "black":
                setTheme(R.style.CarnetTheme_Darkest);
                break;

        }
        mSavedInstanceState = savedInstanceState;
        if(PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceHelper.NOTE_VERSION_PREF, -1)==-1){
            //not set but already start  : V1
            PreferenceHelper.setCurrentNoteVersion(getApplicationContext(),1);
        }
        setContentView(R.layout.activity_main);
        mLockLayout = (FrameLayout)findViewById(R.id.lock_layout);
        onNewIntent(getIntent());
        if(!UpdaterActivity.startUpdateIfNeeded(this, UPDATE_REQUEST_CODE)){
            onUpdateDone();
        }
        new RemindersManager(this);
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
        final MenuItem item = menu.findItem(R.string.sync);
        item.setEnabled(!SynchroService.isSyncing);
        if(SynchroService.isSyncing){
            TypedArray a = getTheme().obtainStyledAttributes(new int[] {R.attr.SyncDisabled});
            int attributeResourceId = a.getResourceId(0, 0);
            Drawable drawable = getResources().getDrawable(attributeResourceId);
            item.setIcon(attributeResourceId);
            mLockLayout.post(new Runnable() {
                @Override
                public void run() {
                    ((Animatable) item.getIcon()).start();
                }
            });
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
                Intent intent = new Intent(this, HelpActivity.class);
                intent.putExtra(HelpActivity.SYNC_ONLY, true);
                startActivity(intent);
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

    @Override
    public void onSyncFailure(String errorMessage) {

    }

    @Override
    public void onSyncSuccess() {

    }

    private void onUpdateDone() {
        try {
            Cursor cursor = DBAccountHelper.getInstance(this).getCursor();
            if (cursor != null && cursor.getCount() > 1 && !PreferenceManager.getDefaultSharedPreferences(this).getBoolean("has_warn_multiple_accounts", false)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.multiple_accounts_warning);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MainActivity.this, AccountListActivity.class));
                    }
                });
                builder.show();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("has_warn_multiple_accounts", true).commit();
            }
        } catch(Exception e){
            Log.e("MainActivity","error on update", e);
        }
        if(!DBMergerService.isJobScheduledOrRunning(this)){
            DBMergerService.scheduleJob(this,true, DBMergerService.ALL_DATABASES);
        }
        int count = PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceHelper.LAUNCH_COUNT, 1);

        if(count%20 == 0 && !PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(PreferenceHelper.HAS_RATED, false)){
            Snackbar.make(findViewById(R.id.root), R.string.rate_ask,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.rate, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(PreferenceHelper.HAS_RATED, true).commit();
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spisoft.quicknote"));
                            startActivity(browserIntent);
                        }
                    })
                    .setDuration(5000)
                    .show();
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(PreferenceHelper.LAUNCH_COUNT, count+1).commit();


        boolean fragmentIsHandled = handleNewIntent();
        startService(new Intent(this, CacheBuilderIntentService.class));
        lockOnStart= true;
        if(mSavedInstanceState==null) {
            if(!fragmentIsHandled) {
                Fragment fragment = MainFragment.newInstance();
                mFragmentToPut = fragment;
                setFragment(mFragmentToPut);
            }
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
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_faster_editor_load", false) && !fragmentIsHandled) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setPreloadBlankFragment();

                }
            }, 1000);
        }
        mIsUpdateDone = true;
    }

    public void openDonation(View view) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(PreferenceHelper.HAS_DONATE, true).commit();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://donation.carnet.live"));
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
        if(!isChangingConfigurations()) {
            try {
                startService(new Intent(this, SynchroService.class));
            } catch (java.lang.IllegalStateException e){ //should happen when foreground... but it does

            }
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

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        mNewIntent = intent;
        if(mIsUpdateDone)
            handleNewIntent();
    }

    private boolean handleNewIntent() {
        if(mNewIntent == null)
            return false;
        if(mNewIntent.getAction()==null||!(mNewIntent.getAction().equals(ACTION_OPEN_NOTE) || mNewIntent.getAction().equals(Intent.ACTION_SEND)))
            return false;
        ArrayList<EditorView.Action> actions = new ArrayList<>();
        String path = mNewIntent.getStringExtra(PATH);
        Note note;
        if(path!=null){
            note = new Note(path);
        }
        else {
            String title = mNewIntent.getStringExtra(Intent.EXTRA_SUBJECT) != null ? mNewIntent.getStringExtra(Intent.EXTRA_SUBJECT) + "<br /><br />" : "";
            String text = mNewIntent.getStringExtra(Intent.EXTRA_TEXT) != null ? mNewIntent.getStringExtra(Intent.EXTRA_TEXT) : "";

            note = NoteManager.createNewNote(PreferenceHelper.getRootPath(this));
            RecentHelper.getInstance(this).addNote(note);
            EditorView.Action fillText = new EditorView.Action();
            fillText.type = "prefill";
            fillText.value = title + text;
            actions.add(fillText);
        }
        setFragment(BlankFragment.newInstance(note, actions));
        mNewIntent = null;
        return true;
    }


    public void setFragment(Fragment fragment) {
        if(isFinishing())
            return;
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in,
                R.anim.fade_out);
        Fragment toPut = fragment;
        if(toPut instanceof BlankFragment){
            if(mEditorFrag!=null) {
                mEditorFrag.setArguments(fragment.getArguments());
                toPut = mEditorFrag;
            }
            mEditorFrag = (BlankFragment) toPut;
        }
        try {
            transaction.replace(R.id.root, toPut);
        }
        catch (java.lang.IllegalStateException e){
            if(toPut == mEditorFrag){
                transaction.replace(R.id.root, fragment);
                mEditorFrag = (BlankFragment) fragment;
                toPut = fragment;

            }
        }
        transaction.addToBackStack(toPut.getClass().getName()).commit();
        this.fragment = toPut;

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
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        if(requestCode == UPDATE_REQUEST_CODE){
            onUpdateDone();
        }
        else if(EditorView.sEditorView!=null)
            EditorView.sEditorView.onActivityResult(requestCode, resultCode, data);

    }
    public void setPreloadBlankFragment() {

            if(mEditorFrag==null) {
                mShouldRemove = true;
                try {
                    mEditorFrag = BlankFragment.newInstance(null, null);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .addToBackStack(mEditorFrag.getClass().getName())
                            .replace(R.id.behind_root, mEditorFrag)
                            .commit();
                } catch (IllegalStateException e){
                    mShouldRemove = false;
                }
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

    @Override
    public void superOnBackPressed() {
        getSupportFragmentManager().popBackStackImmediate();
        if(getSupportFragmentManager().getFragments().size() == 0) {
            if(!(this.fragment instanceof BlankFragment)) {
                finish();
            } else{
                setFragment(MainFragment.newInstance());
            }
            return;

        }
        this.fragment  = getSupportFragmentManager().getFragments().get(0);
    }
}
