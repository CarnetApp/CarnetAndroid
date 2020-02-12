package com.spisoft.quicknote.editor;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;

import java.util.ArrayList;

public class ShareEditorActivity extends AppCompatActivity implements EditorActivity{

    public static final String PATH = "note_path";
    public static final String ACTION_OPEN_NOTE = "open_note";
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        onNewIntent(getIntent());

    }


    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        ArrayList<EditorView.Action>actions = new ArrayList<>();
        String path = intent.getStringExtra(PATH);
        Note note;
        if(path!=null){
            note = new Note(path);
        }
        else {
            String title = intent.getStringExtra(Intent.EXTRA_SUBJECT) != null ? intent.getStringExtra(Intent.EXTRA_SUBJECT) + "<br /><br />" : "";
            String text = intent.getStringExtra(Intent.EXTRA_TEXT) != null ? intent.getStringExtra(Intent.EXTRA_TEXT) : "";

            note = NoteManager.createNewNote(PreferenceHelper.getRootPath(this));
            RecentHelper.getInstance(this).addNote(note);
            EditorView.Action fillText = new EditorView.Action();
            fillText.type = "prefill";
            fillText.value = title + text;
            actions.add(fillText);
        }
        setFragment(BlankFragment.newInstance(note, actions));

    }

    public void onBackPressed() {
        if (fragment instanceof com.spisoft.quicknote.editor.BlankFragment)
            if (((com.spisoft.quicknote.editor.BlankFragment) fragment).onBackPressed())
                return;
        finish();
    }

    @Override
    public void superOnBackPressed() {
        finish();
    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.root,fragment);
        transaction.addToBackStack(fragment.getClass().getName()).commit();
        this.fragment = fragment;
    }
}
