package com.spisoft.quicknote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spisoft.quicknote.browser.BrowserFragment;
import com.spisoft.quicknote.browser.KeywordNotesFragment;
import com.spisoft.quicknote.browser.PermissionChecker;
import com.spisoft.quicknote.browser.RecentNoteListFragment;
import com.spisoft.quicknote.browser.SearchFragment;
import com.spisoft.quicknote.databases.KeywordsHelper;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.editor.*;
import com.spisoft.quicknote.editor.BlankFragment;

import java.util.List;
import java.util.Map;

import static com.spisoft.quicknote.MainActivity.ACTION_RELOAD_KEYWORDS;

/**
 */
public class MainFragment extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener {


    private static final String TAG = "MainFragment";
    private View mRoot;
    private DrawerLayout mDrawerLayout;
    private LinearLayout mKeywordsLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private View mBrowserButton;
    private View mRecentButton;
    private View mSettingsButton;
    private BroadcastReceiver mReceiver;
    private KeywordRefreshTask mKeywordsTask;
    private Fragment fragment;
    private IntentFilter mFilter;

    public MainFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(){
        return new MainFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mRoot = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);
        mToolbar = (Toolbar) mRoot.findViewById(R.id.my_awesome_toolbar);
        ((MainActivity)getActivity()).setSupportActionBar(mToolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout)mRoot.findViewById(R.id.drawer_layout);
        mKeywordsLayout = mRoot.findViewById(R.id.keywords);
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mBrowserButton = mRoot.findViewById(R.id.browser_button);
        mBrowserButton.setOnClickListener(this);
        mRecentButton = mRoot.findViewById(R.id.recent_button);
        mRecentButton.setOnClickListener(this);
        mSettingsButton = mRoot.findViewById(R.id.settings_button);
        mSettingsButton.setOnClickListener(this);


        mReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction().equals(ACTION_RELOAD_KEYWORDS)) {
                    if(mKeywordsTask != null)
                        mKeywordsTask.cancel(true);
                    mKeywordsTask = new KeywordRefreshTask();
                    mKeywordsTask.execute();
                }
            }
        };
        mFilter = new IntentFilter();
        mFilter.addAction(FileManagerService.ACTION_COPY_ENDS);
        mFilter.addAction(ACTION_RELOAD_KEYWORDS);
        mFilter.addAction(NoteManager.ACTION_UPDATE_END);

        if(savedInstanceState==null) {

            Fragment fragment = this.fragment!=null?this.fragment:RecentNoteListFragment.newInstance();
            setFragment(fragment);
        }
        return mRoot;
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause");

        getActivity().unregisterReceiver(mReceiver);
        if(mKeywordsTask != null)
            mKeywordsTask.cancel(true);

    }
    
    
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");
        getActivity().registerReceiver(mReceiver, mFilter);

        if(mKeywordsTask != null)
            mKeywordsTask.cancel(true);
        mKeywordsTask = new KeywordRefreshTask();
        mKeywordsTask.execute();

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        SearchView view = (SearchView) menu.findItem(R.id.action_search).getActionView();
        view.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if(fragment instanceof SearchFragment)
            ((SearchFragment) fragment).doSearch(query);
        else {
            Fragment fragment = SearchFragment.newInstance(PreferenceHelper.getRootPath(getActivity()),query);
            setFragment(fragment);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.rootbis,fragment)
                .addToBackStack(null).commit();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
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
            startActivity(new Intent(getContext(), SettingsActivity.class));
            return true;
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            Fragment fragment = BrowserFragment.newInstance(PreferenceHelper.getRootPath(getActivity()));
            setFragment(fragment);
            mDrawerLayout.closeDrawers();
        }
        else if(mSettingsButton==view){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            mDrawerLayout.closeDrawers();
        }
    }

    private void clearBackstack() {

        FragmentManager fm = getChildFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    private void openKeyword(String keyword) {
        clearBackstack();
        Fragment fragment = KeywordNotesFragment.newInstance(keyword);
        setFragment(fragment);
        mDrawerLayout.closeDrawers();

    }

    public void lockDrawer(){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void unlockDrawer(){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public boolean onBackPressed() {
        if (getChildFragmentManager().getBackStackEntryCount() == 1){
            return false;
        }

        getChildFragmentManager().popBackStackImmediate();
        this.fragment  = getChildFragmentManager().getFragments().get(0);
        return true;
    }

    private class KeywordRefreshTask extends AsyncTask<Void, Void, Map<String, List<String>>> {

        @Override
        protected Map<String, List<String>> doInBackground(Void... voids) {

            return KeywordsHelper.getInstance(getActivity()).getFlattenDB(0);
        }

        @Override
        protected void onPostExecute(Map<String, List<String>> result) {
            mKeywordsLayout.removeAllViews();
            for(final Map.Entry<String, List<String>> entry : result.entrySet()){
                if(entry.getValue().size() >0){
                    TextView tv = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.keyword,null);
                    tv.setText(entry.getKey());
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openKeyword(entry.getKey());
                        }
                    });
                    mKeywordsLayout.addView(tv);
                }
            }
        }

    }

}
