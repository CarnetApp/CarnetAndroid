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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment implements View.OnClickListener, EditorView.HideListener, FakeFragmentManager {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String NOTE = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private Note mNote;
    private View mRoot;
    private WebView mWebView;
    private String mNoteString;
    private Stack<FloatingFragment> mFragments ;
    private Handler mHandler = new Handler(){

    };
    private boolean mHasAskedMinimize;
    private EditorView mEditor;
    private ViewGroup mOptionMenuContainer;
    private FrameLayout mfragmentContainer;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
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
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        super.onCreateOptionsMenu( menu,  inflater);
                // Inflate the menu; this adds items to the action bar if it is present.
                menu.add(0, R.string.floating, 0, R.string.floating);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id ==R.string.floating) {
            Intent intent = new Intent(getActivity(), FloatingService.class);
            intent.putExtra(FloatingService.NOTE, mNote);
            getActivity().startService(intent);
            getActivity().finish();
            return true;
        }


        return super.onOptionsItemSelected(item);
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
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            int result = getResources().getDimensionPixelSize(resourceId);
            mRoot.setPadding(0,result, 0,0);
        }
        mEditor = ((EditorView) mRoot.findViewById(R.id.editor_view));
        if(mNote!=null)
            mEditor.setNote(mNote);
        mEditor.reset();
        mEditor.setHideListener(this);

        boolean fullScreen = (getActivity().getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;

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

    public void addFragment(FloatingFragment fragment){
        mfragmentContainer.removeAllViews();
        mFragments.push(fragment);
        mfragmentContainer.addView(fragment.getView());
        mOptionMenuContainer.removeAllViews();
        fragment.setOptionMenu(mOptionMenuContainer);

    }
    public void removeFragment(){
        mfragmentContainer.removeAllViews();
        mFragments.pop();
        FloatingFragment fragment = mFragments.peek();
        mfragmentContainer.addView(fragment.getView());
        mOptionMenuContainer.removeAllViews();
        fragment.setOptionMenu(mOptionMenuContainer);
    }
}
