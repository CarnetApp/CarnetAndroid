package com.spisoft.quicknote.editor;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spisoft.quicknote.FloatingService;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.R;
import com.spisoft.sync.synchro.SynchroService;

import java.util.ArrayList;
import java.util.List;

public class BlankFragment extends Fragment implements View.OnClickListener, EditorView.HideListener {
    public static final String NOTE = "param1";
    public static final String ACTIONS = "ACTIONS";
    private Note mNote;
    private View mRoot;
    private boolean mHasAskedMinimize;
    private EditorView mEditor;
    private List<EditorView.Action> mActions;

    public static BlankFragment newInstance(Note param1, ArrayList<EditorView.Action> actions) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putSerializable(NOTE, param1);
        args.putSerializable(ACTIONS, actions);
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
            mActions = (List<EditorView.Action>) getArguments().getSerializable(ACTIONS);
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
                mEditor.setNote(mNote, mActions);
            return mRoot;
        }
        mRoot = inflater.inflate(R.layout.floating_note, container, false);
        mEditor = ((EditorView) mRoot.findViewById(R.id.editor_view));
        if(mNote!=null)
            mEditor.setNote(mNote, mActions);
        mEditor.reset();
        mEditor.setHideListener(this);

        return mRoot ;
    }

    public boolean onBackPressed(){
        askToExit();
        return true;
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

    public void askToExit(){
        mEditor.askToExit();
    }

    @Override
    public void onExit() {
        if(getActivity()==null) return;
        getActivity().startService(new Intent(getActivity(), SynchroService.class));
        ((EditorActivity)getActivity()).superOnBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(getActivity()==null) return;
            getActivity().startService(new Intent(getActivity(), SynchroService.class));
    }

}
