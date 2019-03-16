package com.spisoft.quicknote.browser;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.spisoft.quicknote.MainActivity;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.databases.CacheManager;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;
import com.spisoft.quicknote.editor.BlankFragment;
import com.spisoft.quicknote.utils.SpiDebugUtils;
import com.spisoft.sync.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 03/02/16.
 */
public class RecentNoteListFragment extends NoteListFragment implements Configuration.PathObserver {
    private Note mTestNote;

    public RecentNoteListFragment(){
        super();
    }

    public static RecentNoteListFragment newInstance() {
        RecentNoteListFragment fragment = new RecentNoteListFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onResume(){
        super.onResume();
        Configuration.addPathObserver(PreferenceHelper.getRootPath(getContext()), this);
    }

    @Override
    public void onPause(){
        super.onPause();
        Configuration.removePathOserver(PreferenceHelper.getRootPath(getContext()), this);
    }

    protected  void onReady(){
        if(SpiDebugUtils.IS_TEST_MODE){
            if(mTestNote == null)
                mTestNote = NoteManager.createNewNote(PreferenceHelper.getRootPath(getActivity()));
        ((MainActivity)getActivity()).setFragment(BlankFragment.newInstance(mTestNote, null));

        }

    }
    @Override
    protected List<Object> getNotes() {
        List<Note> latest = RecentHelper.getInstance(getActivity()).getCachedLatestNotes();
        for(int i = 0; i < latest.size(); i++){
            Note curNote = latest.get(i);
            Note note = CacheManager.getInstance(getContext()).get(curNote.path);
            if(note != null) {
                note.isPinned = curNote.isPinned;
                latest.set(i, note);
            }
        }
        return  new ArrayList<Object>(latest);
    }

    @Override
    protected boolean internalOnMenuClick(MenuItem menuItem, Note note) {
        if(menuItem.getItemId()==R.string.remove_recent){
            RecentHelper.getInstance(getContext()).removeRecent(note);
            reload(null, false);
        }
        else if(menuItem.getItemId()==R.string.pin){
            RecentHelper.getInstance(getContext()).pin(note);
            reload(mLastSelected, false);
        }
        else if(menuItem.getItemId()==R.string.unpin){
            RecentHelper.getInstance(getContext()).unpin(note);
            reload(mLastSelected, false);
        }
        return false;
    }

    @Override
    protected void internalCreateOptionMenu(Menu menu, Note note) {
        menu.add(0, R.string.remove_recent, 0,R.string.remove_recent);
        if(note.isPinned)
            menu.add(0, R.string.unpin, 0,R.string.unpin);
        else
            menu.add(0, R.string.pin, 0,R.string.pin);
    }

    ItemTouchHelper.Callback mSimpleItemTouchHelperCallback = new ItemTouchHelper.Callback (){


        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            RecentHelper.getInstance(getActivity()).moveNote((Note) mNotes.get(viewHolder.getAdapterPosition()), target.getAdapterPosition());
            mNoteAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());

            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            //mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }

    };

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ItemTouchHelper touchHelper = new ItemTouchHelper(mSimpleItemTouchHelperCallback);
        touchHelper.attachToRecyclerView(mRecyclerView);
        mRoot.findViewById(R.id.add_folder_button).setVisibility(View.GONE);
    }

    @Override
    public void onLongClick(Note note, View v) {

    }

    @Override
    public void onPathChanged(String path, List<String> modifiedPaths) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                reload(mLastSelected, true);
            }
        });
    }
}
