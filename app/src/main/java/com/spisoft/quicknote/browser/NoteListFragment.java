package com.spisoft.quicknote.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.spisoft.quicknote.FloatingService;
import com.spisoft.quicknote.MainActivity;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;
import com.spisoft.quicknote.editor.BlankFragment;
import com.spisoft.sync.Configuration;
import com.spisoft.sync.Log;
import com.spisoft.sync.synchro.SynchroService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 03/02/16.
 */
public abstract class NoteListFragment extends Fragment implements NoteAdapter.OnNoteItemClickListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, Configuration.SyncStatusListener {
    public static final String ACTION_RELOAD = "action_reload";
    private static final String TAG = "NoteListFragment";
    protected RecyclerView mRecyclerView;
    protected NoteAdapter mNoteAdapter;
    protected View mRoot;
    public Handler mHandler = new Handler();
    private StaggeredGridLayoutManager mGridLayout;
    protected List<Object> mNotes;
    protected Note mLastSelected;
    private BroadcastReceiver mReceiver;

    private TextView mEmptyViewMessage;
    protected View mEmptyView;
    private boolean mHasLoaded;
    private SwipeRefreshLayout mSwipeLayout;
    private View mProgress;
    private View mCircleView;

    public void onPause(){
        super.onPause();
        myOnPause();
    }

    public void onResume(){
        super.onResume();
        myOnResume();
    }

    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
            if(mRoot!=null)
                return mRoot;
            mRoot = inflater.inflate(R.layout.note_recycler_layout, null);
            mSwipeLayout = (SwipeRefreshLayout) mRoot.findViewById(R.id.swipe_container);
            Field field = null;
            try {
                field = mSwipeLayout.getClass().getDeclaredField("mCircleView");
                field.setAccessible(true);
                mCircleView = (View)field.get(mSwipeLayout);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


            mProgress = mRoot.findViewById(R.id.list_progress);
            mSwipeLayout.setOnRefreshListener(this);
            mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
            mRecyclerView = (RecyclerView) mRoot.findViewById(R.id.recyclerView);
            mEmptyView = mRoot.findViewById(R.id.empty_view);
            mEmptyViewMessage = (TextView) mRoot.findViewById(R.id.empty_message);
            mRoot.findViewById(R.id.add_note_button).setOnClickListener(this);
            mNoteAdapter = getAdapter();
            mNoteAdapter.setOnNoteClickListener(this);
            mGridLayout = new StaggeredGridLayoutManager( 2, StaggeredGridLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(mGridLayout);
            mRecyclerView.setAdapter(mNoteAdapter);



        return mRoot;
    }
    public void hideEmptyView(){
        mEmptyView.setVisibility(View.GONE);
    }

    public void showEmptyMessage(String message){
        mEmptyView.setVisibility(View.VISIBLE);
        if(message!=null)
            mEmptyViewMessage.setText(message);
    }

    public void onViewCreated(View v, Bundle save){
        super.onViewCreated(v, save);
        getActivity().setTitle(R.string.recent);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getActivity()==null || mNotes != null)
                    return;
                mNotes = getNotes();
                if(mNotes!=null) {
                    mNoteAdapter.setNotes(mNotes);
                    if(mNotes.isEmpty())
                        showEmptyMessage(null);
                    else
                        hideEmptyView();
                    if (mLastSelected != null && mNotes.indexOf(mLastSelected) > 0)
                        mGridLayout.scrollToPosition(mNotes.indexOf(mLastSelected));
                    else
                        mGridLayout.scrollToPosition(0);
                }
                onReady();
            }
        }, 0);
        mReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                //requestMinimize();
                if(intent.getAction().equals(ACTION_RELOAD)||intent.getAction().equals(NoteManager.ACTION_UPDATE_END)){
                    boolean reloadAll = false;
                    if(intent.getAction().equals(ACTION_RELOAD) && intent.getSerializableExtra("notes")!=null && mNotes!=null){//reload only specified notes
                        List<Note> notes = (List<Note>) intent.getSerializableExtra("notes");
                        for(Note note : notes){
                            int index;
                            if((index = mNotes.indexOf(note))>=0){
                                ((Note)mNotes.get(index)).needsUpdateInfo = true;
                                mNoteAdapter.notifyItemChanged(index);
                            }else{
                                reloadAll = true;
                                break;
                            }
                        }
                    } else reloadAll = true;

                    if(reloadAll)
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            reload(mLastSelected);

                        }
                    }, 500);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RELOAD);
        filter.addAction(NoteManager.ACTION_UPDATE_END);

        getActivity().registerReceiver(mReceiver, filter);


    }

    protected  void onReady(){}

    protected void reload(Note scrollTo) {
        mNotes = getNotes();
        if(mNotes!=null) {
            mNoteAdapter.setNotes(mNotes);
            if (mNotes != null) {
                if(mNotes.isEmpty())
                    showEmptyMessage(null);
                else
                    hideEmptyView();
            }
            if (scrollTo != null && mNotes.indexOf(scrollTo) > 0)
                mGridLayout.scrollToPosition(mNotes.indexOf(scrollTo));
            else
                mGridLayout.scrollToPosition(0);
        }
        onReady();

    }

    public void  onDestroyView(){
        super. onDestroyView();
        getActivity().unregisterReceiver(mReceiver);

    }

    @Override
    public void onRefresh() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("refuse_certificate", false).apply();
        getActivity().startService(new Intent(getActivity(), SynchroService.class));
    }

    @Override
    public void onSyncStatusChanged(boolean isSyncing) {
        mRoot.post(new Runnable() {
            @Override
            public void run() {
                refreshSyncedStatus();
            }
        });
    }

    private void refreshSyncedStatus() {
        mProgress.setVisibility(SynchroService.isSyncing?View.VISIBLE:View.GONE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCircleView.setVisibility(View.GONE);

            }
        },500);
        mSwipeLayout.setRefreshing(SynchroService.isSyncing);
    }

    public void myOnPause() {
        Log.d(TAG, "onPause");
        Configuration.removeSyncStatusListener(this);
    }

    public void myOnResume() {
        Log.d(TAG, "onResume");

        Configuration.addSyncStatusListener(this);
        refreshSyncedStatus();
        if(mLastSelected != null && mNotes != null){
            int index;
            if((index = mNotes.indexOf(mLastSelected))>=0){
                ((Note)mNotes.get(index)).needsUpdateInfo = true;
                mNoteAdapter.notifyItemChanged(index);
                Log.d(TAG, "notifyItemChanged");

            }
            else
                reload(mLastSelected);
            mLastSelected = null;
        }
    }

    public class ReadReturnStruct{
        boolean hasFound;
        String readText;
        List<String> keyWords;
    }



    public  NoteAdapter getAdapter(){
        return new NoteAdapter(getActivity(),new ArrayList<Object>());
    }

    protected abstract List<Object> getNotes();

    @Override
    public void onNoteClick(Note note, View view) {
        mLastSelected = note;
        /*if(Build.VERSION.SDK_INT>=  Build.VERSION_CODES.M&&!Settings.canDrawOverlays(getActivity())){
            Intent intent = new Intent(getContext(), HelpAuthorizeFloatingWindowActivity.class);
            intent.putExtra(FloatingService.NOTE, note);
            startActivity(intent);

            return;
        }else {*/
        if(NoteManager.needToUpdate(note.path))
            Toast.makeText(getContext(), R.string.please_wait_update, Toast.LENGTH_LONG).show();
        else
            ((MainActivity)getActivity()).setFragment(BlankFragment.newInstance(note));

          /*  Intent intent = new Intent(getActivity(), FloatingService.class);
            intent.putExtra(FloatingService.NOTE, note);
            getActivity().startService(intent);*/
        //}
    }
    @Override
    public void onInfoClick(final Note note, View view){
        PopupMenu menu = new PopupMenu(getActivity(), view);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(menuItem.getItemId()== R.string.delete){
                    if(FloatingService.sService!=null&&FloatingService.sService.getNote()!=null&&FloatingService.sService.getNote().path.equalsIgnoreCase(note.path)){

                        Toast.makeText(getActivity(), R.string.unable_to_delete_use, Toast.LENGTH_LONG).show();
                        return true;
                    }
                    NoteManager.deleteNote(getContext(), note);
                    mNotes = getNotes();
                    mNoteAdapter.setNotes((List<Object>) mNotes);


                }else if(menuItem.getItemId() == R.string.rename){
                    if(FloatingService.sService!=null&&FloatingService.sService.getNote()!=null&&FloatingService.sService.getNote().path.equalsIgnoreCase(note.path)){

                        Toast.makeText(getActivity(), R.string.unable_to_rename_use, Toast.LENGTH_LONG).show();
                        return true;
                    }
                    RenameDialog dialog = new RenameDialog();
                    dialog.setName(note.title);
                    dialog.setRenameListener(new RenameDialog.OnRenameListener() {
                        @Override
                        public boolean renameTo(String name) {
                            boolean success = NoteManager.renameNote(getContext(), note, name+".sqd") != null;
                            reload(mLastSelected);
                            return success;

                        }
                    });
                    dialog.show(getFragmentManager(), "rename");
                }
                return internalOnMenuClick(menuItem, note);
            }
        });
        menu.getMenu().add(0, R.string.rename, 0, R.string.rename);
        menu.getMenu().add(0, R.string.delete, 0, R.string.delete);
        internalCreateOptionMenu(menu.getMenu(), note);
        menu.show();
    }

    protected abstract boolean internalOnMenuClick(MenuItem menuItem, Note note);

    protected abstract void internalCreateOptionMenu(Menu menu, Note note);

    protected void createAndOpenNewNote(String path){
        Note note = NoteManager.createNewNote(path);
        RecentHelper.getInstance(getContext()).addNote(note);
        mLastSelected = note;
        ((MainActivity)getActivity()).setFragment(BlankFragment.newInstance(note));
    }

    @Override
    public void onClick(View view) {
        if(view==mRoot.findViewById(R.id.add_note_button)) {
              createAndOpenNewNote(PreferenceHelper.getRootPath(getActivity()));
        }
    }



}
