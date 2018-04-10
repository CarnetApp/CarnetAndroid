package com.spisoft.quicknote.browser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.spisoft.quicknote.FileManagerService;
import com.spisoft.quicknote.FloatingService;
import com.spisoft.quicknote.MainActivity;
import com.spisoft.quicknote.MainFragment;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.editor.BlankFragment;
import com.spisoft.quicknote.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 05/02/16.
 */
public class BrowserFragment extends NoteListFragment implements BrowserAdapter.OnFolderClickListener {

    private static final String PATH = "path";
    private String mPath;
    private boolean hasAddedButton;
    private  ActionMode.Callback mActionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
           // menu.add(0,R.string.delete, 0,R.string.delete);
            menu.add(0,R.string.move, 0,R.string.move);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.string.move:
                    PasteBin.clear();
                    PasteBin.addObjects(mNoteAdapter.getSelectedObjects());
                    Toast.makeText(getActivity(), R.string.paste_how_to, Toast.LENGTH_LONG).show();
                    mActionMode.finish();
                    getActivity().invalidateOptionsMenu();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mNoteAdapter.clearSelection();
            mActionMode=null;
        }
    };
    private ActionMode mActionMode;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(PasteBin.getPasteBin().size()>0)
            menu.add(0,R.string.paste, 0,R.string.paste);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.string.paste:
                ((MainActivity)getActivity()).displayPasteDialog();
                FileManagerService.startCopy(getActivity(), PasteBin.getPasteBin(), mPath);
                PasteBin.clear();
                getActivity().invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public static BrowserFragment newInstance(String path) {
        BrowserFragment fragment = new BrowserFragment();
        Bundle args = new Bundle();
        args.putString(PATH, path);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    protected List<Object> getNotes() {
        mPath = getArguments().getString(PATH);
        boolean avoidDbFolder = mPath.equals(PreferenceHelper.getRootPath(getActivity()));
        File file = new File(mPath);
        List<Object> ret = new ArrayList<>();
        List<Object> dir = new ArrayList<>();
        List<Object> notes = new ArrayList<>();
        if(file.exists()){
            File[] files = file.listFiles();
            if(files!=null){
                for(File file1 : files){
                    if(file1.getName().startsWith(".")||avoidDbFolder&&file1.getName().equals("quickdoc"))
                        continue;
                    if(file1.isFile()&&file1.getName().endsWith(".sqd")){
                        notes.add(new Note(file1.getAbsolutePath()));
                    }
                    else if(file1.isDirectory())
                        dir.add(file1);


                }
            }
            ret.addAll(dir);
            ret.addAll(notes);
        }
        return ret;
    }
    public void onViewCreated(View v, Bundle save) {
        super.onViewCreated(v, save);
        setHasOptionsMenu(true);
        if(mRoot.findViewById(R.id.add_note_button) == null)
            addSecondaryButton(R.layout.browser_secondary_buttons);
        hasAddedButton = true;
        mRoot.findViewById(R.id.add_note_button).setOnClickListener(this);
        mRoot.findViewById(R.id.add_folder_button).setOnClickListener(this);
        getActivity().setTitle(R.string.browser);
        //mRoot.
    }
    @Override
    public void onClick(View view) {
        if(view==mRoot.findViewById(R.id.add_button)) {
            super.onClick(view);
            /*File file = new File(PreferenceHelper.getRootPath(getActivity()));
            String path = mPath + (!mPath.endsWith("/")?"/":"")+"untitled";
            Fragment fragment = BlankFragment.newInstance(new Note(path));
            ((MainActivity) getActivity()).setFragment(fragment);*/
        }else if(view == mRoot.findViewById(R.id.add_note_button)){
            createAndOpenNewNote(mPath);
        }else if(view == mRoot.findViewById(R.id.add_folder_button)){
            RenameDialog dialog = new RenameDialog();
            dialog.setName(getString(R.string.new_folder_name));
            dialog.setRenameListener(new RenameDialog.OnRenameListener() {
                @Override
                public boolean renameTo(String name) {
                    File newFolder = new File(mPath.endsWith("/") ? mPath : (mPath + "/") + name);
                    boolean isOk = !(newFolder).exists();
                    if (isOk) {
                        newFolder.mkdir();
                        reload();
                    }
                    return isOk;

                }
            });
            dialog.show(getFragmentManager(), "rename");
        }
    }
    public  NoteAdapter getAdapter(){
        BrowserAdapter adapter = new BrowserAdapter(getActivity(),new ArrayList<Object>());
        adapter.setOnFolderClickListener(this);


        return adapter;
    }
    @Override
    protected boolean internalOnMenuClick(MenuItem menuItem, Note note) {
        return false;
    }

    @Override
    protected void internalCreateOptionMenu(Menu menu) {

    }

    @Override
    public void onFolderClick(File folder) {
        Fragment fragment = BrowserFragment.newInstance(folder.getAbsolutePath());
        ((MainFragment)getParentFragment()).setFragment(fragment);
    }

    @Override
    public void onFolderOptionClick(final File note, View view) {
        PopupMenu menu = new PopupMenu(getActivity(), view);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(menuItem.getItemId()== R.string.delete){
                    if(FloatingService.sService!=null&&FloatingService.sService.getNote()!=null&&FloatingService.sService.getNote().path.startsWith(note.getAbsolutePath())){

                        Toast.makeText(getActivity(), R.string.unable_to_delete_use, Toast.LENGTH_LONG).show();
                        return true;
                    }
                    FileUtils.deleteRecursive(new File(note.getAbsolutePath()));
                    reload();
                }else if(menuItem.getItemId() == R.string.rename){
                    if(FloatingService.sService!=null&&FloatingService.sService.getNote()!=null&&FloatingService.sService.getNote().path.startsWith(note.getAbsolutePath())){

                        Toast.makeText(getActivity(), R.string.unable_to_rename_use, Toast.LENGTH_LONG).show();
                        return true;
                    }
                    RenameDialog dialog = new RenameDialog();
                    dialog.setName(note.getName());
                    dialog.setRenameListener(new RenameDialog.OnRenameListener() {
                        @Override
                        public boolean renameTo(String name) {
                            boolean success = FileUtils.renameDirectory(getContext(), note, name) != null;
                            reload();
                            return true;

                        }
                    });
                    dialog.show(getFragmentManager(), "rename");
                }
                // return internalOnMenuClick(menuItem, note);
                return true;
            }
        });
        menu.getMenu().add(0, R.string.rename, 0, R.string.rename);
        menu.getMenu().add(0, R.string.delete, 0, R.string.delete);
        internalCreateOptionMenu(menu.getMenu());
        menu.show();
    }

    @Override
    public void onLongClick(File note, View view) {
        mNoteAdapter.toggleNote(note, view);
        if(mNoteAdapter.getSelectedObjects().size()>0) {
            if (mActionMode == null)
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionCallback);
        }
        else if(mActionMode!=null){
            mActionMode.finish();

        }
    }


    @Override
    public void onLongClick(Note note, View v) {

        mNoteAdapter.toggleNote(note, v);
        if(mNoteAdapter.getSelectedObjects().size()>0) {
            if (mActionMode == null)
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionCallback);
        }
        else if(mActionMode!=null){
            mActionMode.finish();

        }
    }


}
