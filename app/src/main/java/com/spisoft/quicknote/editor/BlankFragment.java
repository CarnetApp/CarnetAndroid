package com.spisoft.quicknote.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import com.spisoft.quicknote.FloatingFragment;
import com.spisoft.quicknote.FloatingService;
import com.spisoft.quicknote.MainActivity;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.R;

import java.util.Stack;

public class BlankFragment extends Fragment implements View.OnClickListener, EditorView.HideListener {
    public static final String NOTE = "param1";
    private Note mNote;
    private View mRoot;
    private boolean mHasAskedMinimize;
    private EditorView mEditor;

    public static BlankFragment newInstance(Note param1) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putSerializable(NOTE, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public BlankFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNote = (Note) getArguments().getSerializable(NOTE);
        }
        setHasOptionsMenu(true);

    }
    public void setNote(Note note){
        mNote = note;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(mRoot!=null) {
            if(mNote!=null)
                mEditor.setNote(mNote);
            return mRoot;
        }
        mRoot = inflater.inflate(R.layout.floating_note, container, false);
        mEditor = ((EditorView) mRoot.findViewById(R.id.editor_view));
        if(mNote!=null)
            mEditor.setNote(mNote);
        mEditor.reset();
        mEditor.setHideListener(this);

        return mRoot ;
    }

    public boolean onBackPressed(){

        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().stopService(new Intent(getContext(), FloatingService.class));
    }

    @Override
    public void onDetach() {
        super.onDetach();
       mEditor.onDestroy();
        //((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mHasAskedMinimize){
            if(Build.VERSION.SDK_INT>=  Build.VERSION_CODES.M&&Settings.canDrawOverlays(getActivity())){
                startFloating();
            }
        }

        mHasAskedMinimize = false;
    }
    @Override
    public void onClick(View view) {


    }

    private void startFloating() {
        Intent intent = new Intent(getActivity(), FloatingService.class);
        intent.putExtra(FloatingService.NOTE,mNote);
        intent.putExtra(FloatingService.START_MINIMIZE,true);
        getActivity().startService(intent);
        getActivity().onBackPressed();
    }

    @Override
    public void onHide(boolean hide) {

    }

    @Override
    public void onExit() {
        Log.d("exitdebug", "onExit");
        if(getActivity()!=null)
        getActivity().onBackPressed();
    }

}
