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
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;
import com.spisoft.quicknote.editor.BlankFragment;
import com.spisoft.quicknote.utils.SpiDebugUtils;

import java.util.List;

/**
 * Created by alexandre on 03/02/16.
 */
public class RecentNoteListFragment extends NoteListFragment {
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
    protected  void onReady(){
        if(SpiDebugUtils.IS_TEST_MODE){
            if(mTestNote == null)
                mTestNote = NoteManager.createNewNote(PreferenceHelper.getRootPath(getActivity()));
        ((MainActivity)getActivity()).setFragment(BlankFragment.newInstance(mTestNote));

        }

    }
    @Override
    protected List<Object> getNotes() {
        return  RecentHelper.getInstance(getActivity()).getLatestNotes(-1);
    }

    @Override
    protected boolean internalOnMenuClick(MenuItem menuItem, Note note) {
        if(menuItem.getItemId()==R.string.remove_recent){
            RecentHelper.getInstance(getContext()).removeRecent(note);
            mNotes = getNotes();
            mNoteAdapter.setNotes((List<Object>) mNotes);
        }
        else if(menuItem.getItemId()==R.string.pin){
            RecentHelper.getInstance(getContext()).pin(note);
            reload(mLastSelected);
        }
        else if(menuItem.getItemId()==R.string.unpin){
            RecentHelper.getInstance(getContext()).unpin(note);
            reload(mLastSelected);
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
    }

    @Override
    public void onLongClick(Note note, View v) {

    }
}
