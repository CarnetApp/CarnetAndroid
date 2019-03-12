package com.spisoft.quicknote.browser;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.databases.NoteManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.os.Handler;

/**
 * Created by alexandre on 03/02/16.
 */
public class NoteAdapter extends RecyclerView.Adapter implements NoteInfoRetriever.NoteInfoListener {

    private static final int NOTE = 0;
    protected final Context mContext;
    private final HashMap<Note, String> mText;
    private final float mBigText;
    private final float mMediumText;
    private final float mSmallText;
    private final NoteInfoRetriever mNoteInfoRetriever;
    private final NoteThumbnailEngine mNoteThumbnailEngine;

    private final Handler mHandler;
    private final String mLoadingText;
    private final Resources.Theme mTheme;
    protected List<Object> mNotes;
    private ArrayList<Object> mSelelectedNotes;

    public NoteAdapter(Context context, List<Object> notes) {
        super();
        mContext = context;
        mHandler = new Handler();
        mNotes = notes;
        mText = new HashMap<Note, String>();
        mBigText = mContext.getResources().getDimension(R.dimen.big_text);
        mMediumText = mContext.getResources().getDimension(R.dimen.medium_text);
        mSmallText = mContext.getResources().getDimension(R.dimen.small_text);
        mNoteInfoRetriever = new NoteInfoRetriever(this, context);
        mNoteThumbnailEngine = new NoteThumbnailEngine(context);
        mLoadingText = context.getResources().getString(R.string.loading);
        mTheme = context.getTheme();

    }

    public void setNotes(List<Object> notes) {
        int i = 0;
        List<Object> oldNotes = new ArrayList<>(mNotes);
        List<Object> oldNotes1 = new ArrayList<>(mNotes);
        List<Object> newNotes = new ArrayList<>(notes);
        List<Object> toRemove = new ArrayList<>();
        mNotes = notes;
        for (Object note : oldNotes1) {
            if (!notes.contains(note)) {
                i = oldNotes.indexOf(note);
                oldNotes.remove(i);
                notifyItemRemoved(i);
            }
        }
        for (Object note : notes) {
            if (!oldNotes.contains(note)) {
                notifyItemInserted(oldNotes.size());
                oldNotes.add(note);
            }
        }
        i = 0;
        oldNotes1 = new ArrayList<>(oldNotes);
        for (Object note : oldNotes) {
            int newPos = notes.indexOf(note);
            int currentPos = oldNotes1.indexOf(note);
            if (newPos >= 0) {
                if(newPos != currentPos) {
                    newNotes.remove(note);
                    oldNotes1.remove(currentPos);
                    if(newPos<oldNotes1.size())
                        oldNotes1.add(newPos, note);
                    else
                        oldNotes1.add(note);
                    notifyItemMoved(currentPos, newPos);
                }
                if(note instanceof Note && notes.get(i) instanceof Note && ((Note)note).isPinned != ((Note)notes.get(i)).isPinned){
                    notifyItemChanged(newPos);
                }

            }
            if (newPos == -1)
                toRemove.add(note);

            i++;
        }
    }

    public void invalidateNotesMetadata(){
        for(Object note: mNotes){
            if(note instanceof Note){
                ((Note) note).needsUpdateInfo = true;
            }
        }
        notifyDataSetChanged();
    }


    public void addNote(Object note) {
        mNotes.add(note);
        notifyItemInserted(mNotes.size() - 1);
    }

    public void setText(Note note, String text) {
        int index = mNotes.indexOf(note);
        if(index>=0) {
            Note oldNote = (Note) mNotes.get(index);
            boolean hasChanged = (text != null && text.equals(mText.get(note))) || !note.mMetadata.equals(oldNote.mMetadata) || !note.previews.equals(oldNote.previews) || !note.medias.equals(oldNote.medias);
            if(hasChanged) {
                mText.put(note, text);
                mNotes.remove(index);
                mNotes.add(index, note);
                notifyItemChanged(index);
            }
        }

    }

    public void onItemMove(int adapterPosition, int adapterPosition1) {
        Note note = (Note) mNotes.get(adapterPosition);
        mNotes.remove(adapterPosition);
        mNotes.add(adapterPosition1, note);
        notifyItemMoved(adapterPosition, adapterPosition1);
    }

    public void toggleNote(Object note, View v) {
        if (mSelelectedNotes == null)
            mSelelectedNotes = new ArrayList<>();
        if (mSelelectedNotes.contains(note)) {
            Log.d("selectdebug", "remove");
            mSelelectedNotes.remove(note);
        } else {
            Log.d("selectdebug", "add");
            mSelelectedNotes.add(note);
        }
        notifyItemChanged(mNotes.indexOf(note));
    }

    public List<Object> getSelectedObjects() {
        return mSelelectedNotes;
    }

    public void clearSelection() {
        if (mSelelectedNotes != null)
            mSelelectedNotes.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onNoteInfo(Note note) {
        setText(note, note.shortText);
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {


        private final View mMainView;
        private final TextView mTitleView;
        private final TextView mTextView;
        private final TextView mMarkView;
        private final ImageView mPreview1;
        private final ImageView mPreview2;
        private final CardView mCard;
        private final LinearLayout mUrlContainer;
        private Note mNote;

        public NoteViewHolder(View itemView) {
            super(itemView);
            mMainView = itemView.findViewById(R.id.cardview);
            mCard = itemView.findViewById(R.id.rootcardview);
            mTitleView = (TextView) itemView.findViewById(R.id.name_tv);
            mTextView = (TextView) itemView.findViewById(R.id.text_tv);
            mMarkView = (TextView) itemView.findViewById(R.id.mark_tv);
            mPreview1= (ImageView) itemView.findViewById(R.id.preview1);
            mPreview2 = (ImageView) itemView.findViewById(R.id.preview2);
            mUrlContainer = (LinearLayout) itemView.findViewById(R.id.url_container);
        }

        public void setName(String title) {
            if(title.startsWith("untitled"))
                mTitleView.setVisibility(View.GONE);
            else {
                mTitleView.setVisibility(View.VISIBLE);
                mTitleView.setText(title);
            }
        }

        public void setNote(final Note note) {
            if(!note.equals(getNote())){
                //reset preview
                setPreview1(null);
                setPreview2(null);
            }
            mNote = note;
            mMainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSelelectedNotes != null && mSelelectedNotes.size() > 0)
                        mOnNoteClick.onLongClick(note, view);
                    else
                        mOnNoteClick.onNoteClick(note, view);
                }
            });
            mMainView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOnNoteClick.onLongClick(note, view);
                    return true;
                }
            });
            itemView.findViewById(R.id.optionsButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnNoteClick.onInfoClick(note, view);
                }
            });
            setName(note.title);
            setText(note.shortText);
            setRating(note.mMetadata.rating);
            setDate(note.mMetadata.custom_date!=-1?note.mMetadata.custom_date:note.mMetadata.last_modification_date);
            setKeywords(note.mMetadata.keywords);
            setBackground(note.mMetadata.color);
            setTodoLists(note.mMetadata.todolists);
            setUrls(note.mMetadata.urls);
        }

        private void setUrls(List<String> urls){
            mUrlContainer.removeAllViews();
            for(final String url : urls){
                View cont = LayoutInflater.from(mContext).inflate(R.layout.url_layout, mUrlContainer, false);
                TextView tv = cont.findViewById(R.id.textview);
                tv.setText(url);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnNoteClick.onUrlClick(url);
                    }
                });
                mUrlContainer.addView(cont);
            }
        }

        private void setTodoLists(List<Note.TodoList> todolists) {
            ViewGroup container = mMainView.findViewById(R.id.todolist_items_container);
            container.removeAllViews();
            for(final Note.TodoList todoList:todolists){
                for(final String item : todoList.todo){
                    final CheckBox checkBox = new CheckBox(mContext);
                    checkBox.setText(item);
                    container.addView(checkBox);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            todoList.todo.remove(item);
                            todoList.done.add(item);
                            NoteManager.updateMetadata(mContext, mNote);
                            checkBox.post(new Runnable() {
                                @Override
                                public void run() {
                                    ((LinearLayout)checkBox.getParent()).removeView(checkBox);
                                }
                            });
                        }
                    });

                }
            }
        }

        public void setBackground(String color) {
            int attr = R.attr.NoteBGNone;
            if(color!=null)
            switch (color){
                case "red":
                    attr = R.attr.NoteBGRed;
                    break;
                case "orange":
                    attr = R.attr.NoteBGOrange;
                    break;
                case "yellow":
                    attr = R.attr.NoteBGYellow;
                    break;
                case "green":
                    attr = R.attr.NoteBGGreen;
                    break;
                case "teal":
                    attr = R.attr.NoteBGTeal;
                    break;
                case "blue":
                    attr = R.attr.NoteBGBlue;
                    break;
                case "violet":
                    attr = R.attr.NoteBGViolet;
                    break;
                case "purple":
                    attr = R.attr.NoteBGPurple;
                    break;
                case "pink":
                    attr = R.attr.NoteBGPink;
                    break;
            }
            TypedValue typedValue = new TypedValue();

            mTheme.resolveAttribute(attr, typedValue, true);
            @ColorInt int colorInt = typedValue.data;
            mCard.setCardBackgroundColor(colorInt);

            Drawable background = mMainView.getBackground();
            if (background instanceof ShapeDrawable) {
                // cast to 'ShapeDrawable'
                ShapeDrawable shapeDrawable = (ShapeDrawable) background;
                shapeDrawable.getPaint().setColor(colorInt);
            } else if (background instanceof GradientDrawable) {
                // cast to 'GradientDrawable'
                GradientDrawable gradientDrawable = (GradientDrawable) background;
                gradientDrawable.setColor(colorInt);
            } else if (background instanceof ColorDrawable) {
                // alpha value may need to be set again after this call
                ColorDrawable colorDrawable = (ColorDrawable) background;
                colorDrawable.setColor(colorInt);
            }

        }
        public void setPreview1(Bitmap bitmap) {
            if(bitmap == null)
                mPreview1.setVisibility(View.GONE);
            else
                mPreview1.setVisibility(View.VISIBLE);
            mPreview1.setImageBitmap(bitmap);
        }

        public void setPreview2(Bitmap bitmap) {
            if(bitmap == null)
                mPreview2.setVisibility(View.GONE);
            else
                mPreview2.setVisibility(View.VISIBLE);
            mPreview2.setImageBitmap(bitmap);
        }

        public void setText(String s) {
            if(s==null)
                s = "";
            if(mNote.mMetadata.urls.size()>0)//otherwise old notes will appear empty
                s = Pattern.compile("(?:(?:https?|ftp|file):\\/\\/|www\\.|ftp\\.)(?:\\([-A-Z0-9+&@#\\/%=~_|$?!:,.]*\\)|[-A-Z0-9+&@#\\/%=~_|$?!:,.])*(?:\\([-A-Z0-9+&@#\\/%=~_|$?!:,.]*\\)|[A-Z0-9+&@#\\/%=~_|$])", Pattern.CASE_INSENSITIVE).matcher(s).replaceAll("");
            s = s.trim();
            if(s.isEmpty()){
                mTextView.setVisibility(View.GONE);
            }
            else {
                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText(s);
                if (s.length() < 40 && mNote.mMetadata.todolists.size() == 0) {
                    mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mBigText);
                } else if (s.length() < 100 && mNote.mMetadata.todolists.size() == 0) {
                    mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mMediumText);
                } else
                    mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSmallText);
            }

        }
        public void setRating(int rating){
            String ratingStr = "";
            if(rating>=0) ratingStr = " "+rating + "★";
            mMarkView.setText(ratingStr);
        }
        public void setDate(long lastModified) {
            String date = "";

            if (lastModified != -1) {
                date = new SimpleDateFormat("dd/MM/yyyy").format(new Date(lastModified));
            }
            ((TextView) itemView.findViewById(R.id.date_tv)).setText(date);

        }

        public void setKeywords(List<String> keywords) {
            ViewGroup keywordsView = (ViewGroup) itemView.findViewById(R.id.keywords);
            keywordsView.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            for (String keyword : keywords) {
                View keyview = inflater.inflate(R.layout.keyword_item, null);
                ((TextView) keyview.findViewById(R.id.textView)).setText(keyword);
                keywordsView.addView(keyview);

            }
        }

        public void setSelected(boolean contains) {
            mMainView.setSelected(contains);

        }

        public Note getNote() {
            return mNote;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("notedebug", "" + viewType);
        if (viewType == NOTE)
            return new NoteViewHolder(LayoutInflater.from(mContext).inflate(R.layout.grid_note_layout, null));
        else return null;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == NOTE) {
            final Note note = (Note) mNotes.get(position);
            final NoteViewHolder viewHolder = (NoteViewHolder) holder;
            if(viewHolder.getNote()!=null){
                //first we detach it
                mNoteInfoRetriever.cancelNote(viewHolder.getNote().path);
                mNoteThumbnailEngine.cancelNote(viewHolder.getNote());
            }
            viewHolder.setNote(note);

            viewHolder.setSelected(mSelelectedNotes != null && mSelelectedNotes.contains(note));
            Log.d(" ", note.title);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(note.equals(viewHolder.getNote()) && note.needsUpdateInfo)
                        mNoteInfoRetriever.addNote(note.path);
                }
            },500);

            if(note.previews.size()>0){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(note.equals(viewHolder.getNote()))
                            mNoteThumbnailEngine.addNote(note, viewHolder);
                    }
                });
            }

        }

    }

    public int getItemViewType(int position) {
        if (mNotes.get(position) instanceof Note)
            return NOTE;
        return -1;
    }

    OnNoteItemClickListener mOnNoteClick;

    void setOnNoteClickListener(OnNoteItemClickListener listener) {
        mOnNoteClick = listener;
    }

    public interface OnNoteItemClickListener {
        public void onNoteClick(Note note, View v);

        void onInfoClick(Note note, View v);

        void onLongClick(Note note, View v);

        void onUrlClick(String url);
    }

    @Override
    public int getItemCount() {
        Log.d("notedebug", "mNotes " + mNotes.size());

        return mNotes.size();
    }
}
