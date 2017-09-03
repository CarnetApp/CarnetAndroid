package com.spisoft.quicknote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spisoft.quicknote.synchro.googledrive.DBDriveFileHelper;

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


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mListAdapter = new ListAdapter();

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
        mParentFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFile = mFile.getParentFile();
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
                Toast.makeText(getContext(), "ok !" + mFile, Toast.LENGTH_LONG).show();
            PreferenceHelper.setRootPath(getActivity(), mFile.getAbsolutePath());
                DBDriveFileHelper.getInstance(getActivity()).deleteAll();
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
