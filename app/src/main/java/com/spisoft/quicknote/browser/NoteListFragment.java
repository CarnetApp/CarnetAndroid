package com.spisoft.quicknote.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.spisoft.quicknote.FloatingService;
import com.spisoft.quicknote.MainActivity;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.billingutils.BillingUtils;
import com.spisoft.quicknote.billingutils.IsPaidCallback;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;
import com.spisoft.quicknote.editor.BlankFragment;
import com.spisoft.quicknote.server.ZipReaderAndHttpProxy;
import com.spisoft.quicknote.utils.FileUtils;

import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

/**
 * Created by alexandre on 03/02/16.
 */
public abstract class NoteListFragment extends Fragment implements NoteAdapter.OnNoteItemClickListener, View.OnClickListener {
    public static final String ACTION_RELOAD = "action_reload";
    protected RecyclerView mRecyclerView;
    protected NoteAdapter mNoteAdapter;
    protected View mRoot;
    public Handler mHandler = new Handler();
    private StaggeredGridLayoutManager mGridLayout;
    protected List<Object> mNotes;
    private AsyncTask<List<Object>, Note, HashMap<Note, String>> mTextTask;
    private Note mLastSelected;
    private BroadcastReceiver mReceiver;
    private ViewGroup mSecondaryButtonsContainer;
    private boolean mHasSecondaryButtons;

    private TextView mEmptyViewMessage;
    private View mEmptyView;
    private AdView mAdView;
    private boolean mHasLoaded;
    private ZipReaderAndHttpProxy mServer;

    public void onPause(){
        super.onPause();
        mAdView.pause();
    }

    public void onResume(){
        super.onResume();

        if(PreferenceHelper.shouldDisplayAd(getContext())) {
            BillingUtils u = new BillingUtils(getActivity());
            u.checkPayement(new IsPaidCallback(getActivity()) {

                @Override
                public void hasBeenPaid(int isPaid) {
                    super.hasBeenPaid(isPaid);
                    if (!checkPayement(isPaid)) {
                        if(!mHasLoaded) {
                            mHasLoaded = true;
                            AdRequest mAdRequest = new AdRequest.Builder()
                                    .addTestDevice("83AFEFC21FAC0463FBEB3AE8BB750855")
                                    .build();
                            mAdView.loadAd(mAdRequest);
                        }else mAdView.resume();
                        mAdView.setVisibility(View.VISIBLE);
                    }else mAdView.setVisibility(View.GONE);
                }
            });

        }
        else mAdView.setVisibility(View.GONE);

    }

    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        if(mRoot==null) {
            try {
                mServer = new ZipReaderAndHttpProxy(getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRoot = inflater.inflate(R.layout.note_recycler_layout, null);
            mAdView = (AdView)mRoot.findViewById(R.id.adView) ;

            mRecyclerView = (RecyclerView) mRoot.findViewById(R.id.recyclerView);
            mEmptyView = mRoot.findViewById(R.id.empty_view);
            mEmptyViewMessage = (TextView) mRoot.findViewById(R.id.empty_message);
            mRoot.findViewById(R.id.add_button).setOnClickListener(this);
            mSecondaryButtonsContainer = (ViewGroup)mRoot.findViewById(R.id.secondary_buttons);
            mNoteAdapter = getAdapter();
            mNoteAdapter.setOnNoteClickListener(this);
            mGridLayout = new StaggeredGridLayoutManager( 2, StaggeredGridLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(mGridLayout);
            mRecyclerView.setAdapter(mNoteAdapter);

        }


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
    public void addSecondaryButton(View v){
        mHasSecondaryButtons = true;
        mSecondaryButtonsContainer.addView(v);
    } public void addSecondaryButton(int resourceId){
        mHasSecondaryButtons = true;
        LayoutInflater.from(getActivity()).inflate(resourceId, mSecondaryButtonsContainer);
    }

    public void onViewCreated(View v, Bundle save){
        super.onViewCreated(v, save);
        getActivity().setTitle(R.string.recent);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getActivity()==null)
                    return;
                mNotes = getNotes();

                if(mNotes!=null) {

                    mNoteAdapter.setNotes(mNotes);
                    if(mNotes.isEmpty())
                        showEmptyMessage(null);
                    else
                        hideEmptyView();
                    if (mTextTask != null) {
                        mTextTask.cancel(true);
                    }

                    mTextTask = new TextAsyncTask().execute(mNotes);
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

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            reload();

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

    protected void reload() {
        mNotes = getNotes();
        if(mNotes!=null) {

            mNoteAdapter.setNotes(mNotes);
            if (mTextTask != null) {
                mTextTask.cancel(true);
            }
            if (mNotes != null) {
                mTextTask = new TextAsyncTask().execute(mNotes);
                if(mNotes.isEmpty())
                    showEmptyMessage(null);
                else
                    hideEmptyView();
            }
            if (mLastSelected != null && mNotes.indexOf(mLastSelected) > 0)
                mGridLayout.scrollToPosition(mNotes.indexOf(mLastSelected));
            else
                mGridLayout.scrollToPosition(0);
        }
        onReady();

    }

    public void  onDestroyView(){
        super. onDestroyView();
        getActivity().unregisterReceiver(mReceiver);

    }
    public class ReadReturnStruct{
        boolean hasFound;
        String readText;
        List<String> keyWords;
    }


    protected Note getNoteInfo(String path){
        Note note = new Note (path);
        note.setShortText(read(path, 100, 10, null).first);
        Note.Metadata metadata = new Note.Metadata();
        String metadataStr = readZipEntry(mServer.getZipEntry("metadata.json"), -1,-1, null).first;
        if(metadataStr!=null && metadataStr.length()>0){
            metadata = Note.Metadata.fromString(metadataStr);
        }
        note.setMetaData(metadata);
        return note;
    }

    protected Pair<String, Boolean> readZipEntry(ZipEntry entry, long length, int maxLines, String toFind){
        String sb = new String();
        BufferedReader br = null;

        boolean hasFound = toFind == null;
        if(toFind!=null)
            toFind = toFind.toLowerCase();
        try {
            br = new BufferedReader(  br = new BufferedReader(new InputStreamReader(mServer.getZipInputStream(entry))));

            String line = br.readLine();
            long total=0;
            int lines = 0;
            maxLines= 352623523;
            while (line != null) {
                if((total<length||length==-1)&&(lines==-1||lines<maxLines)) {
                    sb += line;
                    sb += "\n";
                }
                total = Jsoup.parse(sb).text().length();
                if(!hasFound){
                    if(line.toLowerCase().contains(toFind)){
                        hasFound = true;
                    }
                }
                line = null;
                lines++;
                if((total<length||length==-1)&&(lines==-1||lines<maxLines)||!hasFound)
                    line = br.readLine();

            }
            sb = Jsoup.parse(sb).text();
            if(!hasFound){
                if(sb.toLowerCase().contains(toFind)){
                    hasFound = true;
                }
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(br!=null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return new Pair<>(sb.toString(), hasFound);
    }

    protected Pair<String, Boolean> read(String path, long length, int maxLines, String toFind){
        mServer.setUri(path);
        path = NoteManager.getHtmlPath(0);
        return readZipEntry(mServer.getZipEntry(path),length, maxLines, toFind);
    }

    public class TextAsyncTask extends AsyncTask<List<Object>,Note, HashMap<Note,String>>{

        protected void onProgressUpdate(Note... values) {
            mNoteAdapter.setText(values[0], values[0].shortText);
        }

        @Override
        protected HashMap<Note, String> doInBackground(List<Object>... lists) {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            List<Object> notes = new ArrayList<>(lists[0]);
            HashMap<Note, String> txts = new HashMap<>();
            for(final Object object : notes){
                if(!(object instanceof  Note))
                    continue;
                Note note = (Note) object;
                final File file = new File(note.path);

                if(file.exists()) {
                    note= getNoteInfo(note.path);
                    note.lastModified = file.lastModified();
                    if (note.mMetadata.creation_date == -1)
                        note.mMetadata.creation_date = file.lastModified();
                    if (note.mMetadata.last_modification_date == -1)
                        note.mMetadata.last_modification_date = file.lastModified();
                    publishProgress(note);
                }

            }

            return txts;
        }
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
                    FileUtils.deleteRecursive(new File(note.path));
                    RecentHelper.getInstance(getContext()).removeRecent(note);
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
                            reload();
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
        internalCreateOptionMenu(menu.getMenu());
        menu.show();
    }

    protected abstract boolean internalOnMenuClick(MenuItem menuItem, Note note);

    protected abstract void internalCreateOptionMenu(Menu menu);

    @Override
    public void onClick(View view) {
        if(view==mRoot.findViewById(R.id.add_button)) {
            if(mHasSecondaryButtons){
                mSecondaryButtonsContainer.setVisibility(mSecondaryButtonsContainer.getVisibility()==View.GONE?View.VISIBLE:View.GONE);
            }
            else {

              /*  if(Build.VERSION.SDK_INT>=  Build.VERSION_CODES.M&&!Settings.canDrawOverlays(getActivity())){
                    Intent intent = new Intent(getContext(), HelpAuthorizeFloatingWindowActivity.class);
                    intent.putExtra(FloatingService.NOTE, NoteManager.createNewNote(PreferenceHelper.getRootPath(getActivity())));
                    startActivity(intent);

                    return;
                }else {*/
                    ((MainActivity)getActivity()).setFragment(BlankFragment.newInstance(NoteManager.createNewNote(PreferenceHelper.getRootPath(getActivity()))));
                    /*Intent intent = new Intent(getActivity(), FloatingService.class);
                    intent.putExtra(FloatingService.NOTE, NoteManager.createNewNote(PreferenceHelper.getRootPath(getActivity())));
                    getActivity().startService(intent);*/
               // }

            }
        }
    }



}
