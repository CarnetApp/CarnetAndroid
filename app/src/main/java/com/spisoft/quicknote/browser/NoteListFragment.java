package com.spisoft.quicknote.browser;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
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
import com.spisoft.quicknote.editor.ImageActivity;
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
            mRoot.findViewById(R.id.add_photos_button).setOnClickListener(this);
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
                reload(null, false);
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
                        refreshNotes((List<Note>) intent.getSerializableExtra("notes"));
                    } else reloadAll = true;

                    if(reloadAll)
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            reload(null, true);

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

    protected void refreshNotes(List<Note> notes){
        boolean reloadAll = false;
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
        if(reloadAll){
            reload(mLastSelected, false);

        }
    }

    private Note createFakeNote(String text, List<String> keywords, String color, int rating, List<String> urls, List<String> todo, List<String> previews){
        Note note = new Note("untitled.sqd");
        note.isFake = true;
        note.mMetadata = new Note.Metadata();
        note.mMetadata.keywords = keywords;
        note.mMetadata.last_modification_date = System.currentTimeMillis();
        note.mMetadata.creation_date = System.currentTimeMillis();
        note.mMetadata.color = color;
        note.mMetadata.rating = rating;
        Note.TodoList todoList = new Note.TodoList();
        todoList.todo = todo;
        note.mMetadata.todolists.add(todoList);
        note.mMetadata.urls.addAll(urls);
        note.shortText = text;
        if(previews != null)
            note.previews.addAll(previews);
        return note;

    }

    protected void reload(Note scrollTo, boolean keepCurrentScroll) {
        mNotes = getNotes();
        if(mNotes == null)
            mNotes = new ArrayList();
        if(mNotes!=null) {
            hideEmptyView();
                if(mNotes.isEmpty() && canDisplayFakeNotes()) {
                    mNotes.add(createFakeNote(getResources().getString(R.string.fake_note_1), new ArrayList<String>(){{

                    }}, "none",5, new ArrayList<String>(), new ArrayList<String>(), null));
                    mNotes.add(createFakeNote(getResources().getString(R.string.fake_note_6), new ArrayList<String>(){{
                    }}, "none",-1, new ArrayList<String>(){{
                        add("https://carnet.live");
                    }}, new ArrayList<String>(){{
                        add(getResources().getString(R.string.fake_note_todo_item_1));
                        add(getResources().getString(R.string.fake_note_todo_item_2));
                    }}, null));

                    mNotes.add(createFakeNote(getResources().getString(R.string.fake_note_5), new ArrayList<String>(){{
                    }}, "red",-1, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(){{add("reader/img/bike.png");}}));

                    mNotes.add(createFakeNote(getResources().getString(R.string.fake_note_2), new ArrayList<String>(){{
                        add("Keyword");
                    }}, "orange",-1, new ArrayList<String>(), new ArrayList<String>(), null));
                    mNotes.add(createFakeNote(getResources().getString(R.string.fake_note_3), new ArrayList<String>(){{
                    }}, "none",3, new ArrayList<String>(), new ArrayList<String>(), null));
                    mNotes.add(createFakeNote(getResources().getString(R.string.fake_note_4), new ArrayList<String>(){{
                    }}, "green",-1, new ArrayList<String>(), new ArrayList<String>(), null));

                }
                else{
                    mNoteAdapter.invalidateNotesMetadata();
                }

            mNoteAdapter.setNotes(mNotes);

            if(keepCurrentScroll) {
            } else if (scrollTo != null && mNotes.indexOf(scrollTo) > 0)
                mGridLayout.scrollToPosition(mNotes.indexOf(scrollTo));
            else
                mGridLayout.scrollToPosition(0);
        }
        onReady();

    }

    protected boolean canDisplayFakeNotes() {
        return true;
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
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCircleView.setVisibility(View.GONE);

            }
        },500);
        mSwipeLayout.setRefreshing(SynchroService.isSyncing);
    }

    public void showProgress(){
        mProgress.setVisibility(View.VISIBLE);
    }

    public void hideProgress(){
        mProgress.setVisibility(View.GONE);
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
                reload(mLastSelected, false);
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
        if(!note.isFake) {
            if (NoteManager.needToUpdate(note.path))
                Toast.makeText(getContext(), R.string.please_wait_update, Toast.LENGTH_LONG).show();
            else
                ((MainActivity) getActivity()).setFragment(BlankFragment.newInstance(note, null));
        } else {
            Toast.makeText(getContext(), R.string.fake_notes_warning, Toast.LENGTH_LONG).show();
        }
          /*  Intent intent = new Intent(getActivity(), FloatingService.class);
            intent.putExtra(FloatingService.NOTE, note);
            getActivity().startService(intent);*/
        //}
    }

    @Override
    public void onUrlClick(String url){
        if(!url.startsWith("http"))
            url = "http://"+url;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void onInfoClick(final Note note, View view){
        if(!note.isFake) {
            PopupMenu menu = new PopupMenu(getActivity(), view);
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    if (menuItem.getItemId() == R.string.delete) {
                        if (FloatingService.sService != null && FloatingService.sService.getNote() != null && FloatingService.sService.getNote().path.equalsIgnoreCase(note.path)) {

                            Toast.makeText(getActivity(), R.string.unable_to_delete_use, Toast.LENGTH_LONG).show();
                            return true;
                        }
                        NoteManager.deleteNote(getContext(), note);
                        reload(null, false);


                    } else if (menuItem.getItemId() == R.string.rename) {
                        if (FloatingService.sService != null && FloatingService.sService.getNote() != null && FloatingService.sService.getNote().path.equalsIgnoreCase(note.path)) {

                            Toast.makeText(getActivity(), R.string.unable_to_rename_use, Toast.LENGTH_LONG).show();
                            return true;
                        }
                        RenameDialog dialog = new RenameDialog();
                        dialog.setName(note.title);
                        dialog.setRenameListener(new RenameDialog.OnRenameListener() {
                            @Override
                            public boolean renameTo(String name) {
                                boolean success = NoteManager.renameNote(getContext(), note, name + ".sqd") != null;
                                reload(mLastSelected, false);
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
        } else {
            Toast.makeText(getContext(), R.string.fake_notes_warning, Toast.LENGTH_LONG).show();
        }
    }

    protected abstract boolean internalOnMenuClick(MenuItem menuItem, Note note);

    protected abstract void internalCreateOptionMenu(Menu menu, Note note);

    protected void createAndOpenNewNote(String path){
        Note note = NoteManager.createNewNote(path);
        RecentHelper.getInstance(getContext()).addNote(note);
        mLastSelected = note;
        ((MainActivity)getActivity()).setFragment(BlankFragment.newInstance(note, null));
    }

    @Override
    public void onClick(View view) {
        if(view==mRoot.findViewById(R.id.add_note_button)) {
              createAndOpenNewNote(PreferenceHelper.getRootPath(getActivity()));
        } else if (view == mRoot.findViewById(R.id.add_photos_button)){
            startActivityForResult(new Intent(getActivity(), ImageActivity.class), 1002);
        }
    }

    @Override
    public void onSyncFailure(String errorMessage){

    }

    @Override
    public void onSyncSuccess(){

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1002 && resultCode == Activity.RESULT_OK){
            reload(null, false);
        }
    }



}
