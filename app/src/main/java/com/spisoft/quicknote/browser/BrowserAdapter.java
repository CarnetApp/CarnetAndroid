package com.spisoft.quicknote.browser;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spisoft.quicknote.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 05/02/16.
 */
public class BrowserAdapter extends NoteAdapter {
    private static final int FOLDER = 1;
    private OnFolderClickListener mOnFolderClickListener;
    private List<Object> mSelelectedFolders;

    public BrowserAdapter(Context context, List<Object> notes) {
        super(context, notes);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==FOLDER)
            return new FolderViewHolder( LayoutInflater.from(mContext).inflate(R.layout.grid_folder_layout, null));
        else return super.onCreateViewHolder(parent,viewType);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType() == FOLDER) {
            File note = (File) mNotes.get(position);
            FolderViewHolder viewHolder = (FolderViewHolder) holder;
            viewHolder.setFile(note);
            viewHolder.setSelected(mSelelectedFolders!=null&&mSelelectedFolders.contains(note));

        }
        else super.onBindViewHolder(holder, position);

    }

    public void toggleNote(Object note, View v) {
        if(note instanceof File) {
            if (mSelelectedFolders == null)
                mSelelectedFolders = new ArrayList<>();
            if (mSelelectedFolders.contains(note)) {
                Log.d("selectdebug", "remove");
                mSelelectedFolders.remove(note);
            } else {
                Log.d("selectdebug", "add");
                mSelelectedFolders.add(note);
            }
            notifyItemChanged(mNotes.indexOf(note));
        }
        else super.toggleNote(note,v);
    }

    public List<Object> getSelectedObjects(){
        List<Object> obj = new ArrayList<>();
        if(super.getSelectedObjects()!=null)
        obj.addAll(super.getSelectedObjects());
        if(mSelelectedFolders!=null)
        obj.addAll(mSelelectedFolders);
        return obj;
    }

    public void clearSelection() {
        if(mSelelectedFolders!=null)
            mSelelectedFolders.clear();
        super.clearSelection();
    }

    public int getItemViewType(int position){
        if(mNotes.get(position) instanceof File)
            return FOLDER;
        return super.getItemViewType(position);
    }
    public void setOnFolderClickListener(OnFolderClickListener listener ){
        mOnFolderClickListener = listener;
    }

    private class FolderViewHolder extends RecyclerView.ViewHolder{
        public FolderViewHolder(View itemView) {
            super(itemView);
        }

        public void setFile(final File note) {
            itemView.findViewById(R.id.optionsButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnFolderClickListener.onFolderOptionClick(note, view);
                }
            });
                    ((TextView) itemView.findViewById(R.id.name_tv)).setText(note.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getSelectedObjects()!=null&&getSelectedObjects().size()>0)
                        mOnFolderClickListener.onLongClick(note, view);
                    else
                    mOnFolderClickListener.onFolderClick(note);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOnFolderClickListener.onLongClick(note, view);
                    return true;
                }
            });
        }
        public void setSelected(boolean contains) {
            itemView.findViewById(R.id.cardview).setSelected(contains);

        }
    }

    public interface OnFolderClickListener {
        public void onFolderClick(File folder);

        void onFolderOptionClick(File note, View view);

        void onLongClick(File note, View view);
    }
}

