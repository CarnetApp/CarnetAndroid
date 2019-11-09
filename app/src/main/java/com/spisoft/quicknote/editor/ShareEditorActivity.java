package com.spisoft.quicknote.editor;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;

import java.util.ArrayList;

public class ShareEditorActivity extends AppCompatActivity implements EditorActivity{

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
        Log.d("sharedebug","on new intent");
        String title = intent.getStringExtra(Intent.EXTRA_SUBJECT) != null ? intent.getStringExtra(Intent.EXTRA_SUBJECT)+"<br /><br />" : "";
        String text = intent.getStringExtra(Intent.EXTRA_TEXT) != null ? intent.getStringExtra(Intent.EXTRA_TEXT) : "";

        Note note = NoteManager.createNewNote(PreferenceHelper.getRootPath(this));
        RecentHelper.getInstance(this).addNote(note);
        EditorView.Action fillText = new EditorView.Action();
        fillText.type="prefill";
        fillText.value=title+text;
        ArrayList<EditorView.Action>actions = new ArrayList<>();
        actions.add(fillText);
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
