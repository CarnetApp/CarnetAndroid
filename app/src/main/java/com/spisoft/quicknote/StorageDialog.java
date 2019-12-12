package com.spisoft.quicknote;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spisoft.sync.account.DBAccountHelper;
import com.spisoft.sync.utils.Utils;
import com.spisoft.sync.wrappers.Wrapper;
import com.spisoft.sync.wrappers.WrapperFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by phoenamandre on 07/02/16.
 */
public class StorageDialog extends DialogFragment implements DialogInterface.OnClickListener, ListingEngine.ListingListener {

    private ListView mListView;
    private ListAdapter mListAdapter;
    private File mFile;
    private View mParentFolderButton;
    private Handler mHandler;
    private TextView mCurrentFolderView;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mListAdapter = new ListAdapter();
        mHandler = new Handler();
        List<File> files = new ArrayList<>();
        files.add(new File(PreferenceHelper.getRootPath(getActivity())));
        //files.add(new File(getContext().get));
        mFile = new File(PreferenceHelper.getRootPath(getActivity()));

        ListingEngine engine = new ListingEngine(mFile, this);
        engine.setFilter(true, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.storage_dialog_layout, null);
        mListView = (ListView) v.findViewById(R.id.listView);
        mListView.setAdapter(mListAdapter);
        mParentFolderButton =v.findViewById(R.id.parent_folder);
        mCurrentFolderView =v.findViewById(R.id.current_folder);
        mCurrentFolderView.setText(mFile.getAbsolutePath());

        mParentFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFile = mFile.getParentFile();
                mCurrentFolderView.setText(mFile.getAbsolutePath());
                ListingEngine engine1 = new ListingEngine(mFile,StorageDialog.this);
                engine1.setFilter(true, null);
                engine1.list();
            }
        });
        refreshParentFolder();
        builder.setView(v);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*
                    update sync
                 */
                Cursor accountCursor = DBAccountHelper.getInstance(getContext()).getCursor();
                if(accountCursor!=null && accountCursor.getCount()>0){
                    int typeCol =accountCursor.getColumnIndex(DBAccountHelper.KEY_ACCOUNT_TYPE);
                    int idCol = accountCursor.getColumnIndex(DBAccountHelper.KEY_ACCOUNT_ID);
                    accountCursor.moveToFirst();
                    do{
                        Wrapper wrapper = WrapperFactory.getWrapper(getActivity(),accountCursor.getInt(typeCol) , accountCursor.getInt(idCol));
                        String remote = wrapper.getRemoteSyncDir(PreferenceHelper.getRootPath(getContext()));
                        wrapper.removeSyncDir(PreferenceHelper.getRootPath(getContext()));
                        wrapper.addFolderSync(mFile.getAbsolutePath(), remote);

                    }while(accountCursor.moveToNext());
                }
                PreferenceHelper.setRootPath(getActivity(), mFile.getAbsolutePath());
                Toast.makeText(getContext(), R.string.app_restart, Toast.LENGTH_LONG).show();

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent mStartActivity = new Intent(Utils.context, MainActivity.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(Utils.context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager)Utils.context.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        System.exit(0);
                    }
                },1000);


            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        engine.list();
        return builder.create();
    }

    private void refreshParentFolder() {
        mParentFolderButton.setVisibility(mFile.equals(Environment.getExternalStorageDirectory())?View.GONE:View.VISIBLE);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        mFile = mListAdapter.getItem(which);
        mCurrentFolderView.setText(mFile.getAbsolutePath());
        ListingEngine engine = new ListingEngine(mFile, this);
        engine.setFilter(true, null);

        engine.list();


    }

    @Override
    public void onFileList(List<File> list) {
        mListAdapter.setData(list);
        Log.d("listdebug", "" + list.size());
        mListAdapter.notifyDataSetChanged();
        refreshParentFolder();
    }

    private class ListAdapter extends BaseAdapter{

        private List<File> mFileList;

        public void setData(List<File> fileList){
            mFileList = fileList;
        }
        @Override
        public int getCount() {
            return mFileList==null?0:mFileList.size();
        }

        @Override
        public File getItem(int position) {
            return mFileList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_folder_layout,null);
            }
            convertView.findViewById(R.id.optionsButton).setVisibility(View.GONE);

            ((TextView)convertView.findViewById(R.id.name_tv)).setText(mFileList.get(position).getName());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StorageDialog.this.onClick(null, position);
                }
            });
            return convertView;
        }


    }
}
