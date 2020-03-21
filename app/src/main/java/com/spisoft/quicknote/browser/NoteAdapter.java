package com.spisoft.quicknote.browser;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.spisoft.quicknote.AudioService;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.utils.Utils;
import com.spisoft.sync.Log;
import com.spisoft.sync.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by alexandre on 03/02/16.
 */
public class NoteAdapter extends RecyclerView.Adapter implements NoteInfoRetriever.NoteInfoListener {

    private static final int NOTE_GRID = 0;
    private static final int NOTE_LINE = 2;
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
    private final float mMaxHeight;
    protected List<Object> mNotes;
    private ArrayList<Object> mSelelectedNotes;
    private boolean mIsInline;

    public NoteAdapter(Context context, List<Object> notes) {
        super();
        mContext = context;
        mHandler = new Handler();
        mNotes = notes;
        mText = new HashMap<Note, String>();
        mBigText = mContext.getResources().getDimension(R.dimen.big_text);
        mMediumText = mContext.getResources().getDimension(R.dimen.medium_text);
        mSmallText = mContext.getResources().getDimension(R.dimen.small_text);
        mMaxHeight = Utils.convertDpToPixel(300, mContext);
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
        for (Object obj : oldNotes) {
            int newPos = notes.indexOf(obj);
            int currentPos = oldNotes1.indexOf(obj);
            if (newPos >= 0) {
                if(newPos != currentPos) {
                    newNotes.remove(obj);
                    oldNotes1.remove(currentPos);
                    if(newPos<oldNotes1.size())
                        oldNotes1.add(newPos, obj);
                    else
                        oldNotes1.add(obj);
                    notifyItemMoved(currentPos, newPos);
                }
                if(obj instanceof Note && notes.get(i) instanceof Note){
                    Note note = (Note) obj;
                    if (note.isPinned != ((Note)notes.get(i)).isPinned || note.pathHasChanged){
                        notifyItemChanged(newPos);
                        note.pathHasChanged = false;
                    }
                }

            }
            if (newPos == -1)
                toRemove.add(obj);
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
            mSelelectedNotes.remove(note);
        } else {
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

    public List getNotes() {
        return mNotes;
    }

    public void setIsInLine(boolean isInLine) {
        mIsInline = isInLine;

    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {


        private final View mMainView;
        private final TextView mTitleView;
        private final TextView mTextView;
        private final TextView mMarkView;
        public final ImageView mPreview1;
        public final ImageView mPreview2;
        private final View mCard;
        private final LinearLayout mUrlContainer;
        private final LinearLayout mAudioContainer;
        private final Button mDisplayMore;
        private final LinearLayout mTodoListContainer;
        private Note mNote;

        public NoteViewHolder(View itemView) {
            super(itemView);
            mMainView = itemView.findViewById(R.id.cardview);
            mCard = itemView.findViewById(R.id.rootcardview);
            mTitleView = itemView.findViewById(R.id.name_tv);
            mTextView = itemView.findViewById(R.id.text_tv);
            mMarkView = itemView.findViewById(R.id.mark_tv);
            mPreview1= itemView.findViewById(R.id.preview1);
            mPreview2 = itemView.findViewById(R.id.preview2);
            mUrlContainer = itemView.findViewById(R.id.url_container);
            mAudioContainer = itemView.findViewById(R.id.audio_container);
            mDisplayMore = itemView.findViewById(R.id.display_more);
            mTodoListContainer = mMainView.findViewById(R.id.todolist_items_container);

            mDisplayMore.setOnClickListener(v -> {
                ViewGroup.LayoutParams params = mTodoListContainer.getLayoutParams();
                if(params.height == WRAP_CONTENT){
                    params.height = (int) mMaxHeight;
                    mDisplayMore.setText(R.string.display_more);
                } else {
                    params.height = WRAP_CONTENT;
                    mDisplayMore.setText(R.string.display_less);
                }
                mTodoListContainer.setLayoutParams(params);
            });
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
                setPreview(mPreview1, null, false);
                setPreview(mPreview2, null, false);
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
            setMedia(note.medias);
            setUrls(note.mMetadata.urls);
        }

        private void setMedia(final ArrayList<String> medias) {
            mAudioContainer.removeAllViews();
            if(medias.size()>0)
                mAudioContainer.setVisibility(View.VISIBLE);
            else
                mAudioContainer.setVisibility(View.GONE);
            for(final String media : medias){
                if(FileUtils.isAudioFile(media)){
                    View cont = LayoutInflater.from(mContext).inflate(R.layout.audio_layout, mUrlContainer, false);
                    View currentButton = null;
                    if(mNote.equals(AudioService.sNote) && media.equals(AudioService.sMedia) && AudioService.isPlaying()){
                        cont.findViewById(R.id.play_button).setVisibility(View.GONE);
                        currentButton = cont.findViewById(R.id.pause_button);
                    }
                    else {
                        cont.findViewById(R.id.pause_button).setVisibility(View.GONE);
                        currentButton = cont.findViewById(R.id.play_button);
                    }
                    currentButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnNoteClick.onAudioClick(mNote, media);
                        }
                    });
                    TextView tv = cont.findViewById(R.id.textview);
                    tv.setText(new File(media).getName());

                    mAudioContainer.addView(cont);

                }

            }
        }

        private void setUrls(List<String> urls){
            mUrlContainer.removeAllViews();
            if(urls.size()>0)
                mUrlContainer.setVisibility(View.VISIBLE);
            else
                mUrlContainer.setVisibility(View.GONE);
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
            mTodoListContainer.removeAllViews();
            for(final Note.TodoList todoList:todolists){
                for(final String item : todoList.todo){
                    final RelativeLayout cblayout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.todolist_item, mTodoListContainer, false);
                    final CheckBox checkBox = cblayout.findViewById(R.id.checkbox);
                    final TextView tv = cblayout.findViewById(R.id.textview);
                    tv.setText(item);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            todoList.todo.remove(item);
                            todoList.done.add(item);
                            NoteManager.updateMetadata(mContext, mNote);
                            cblayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    ((LinearLayout)cblayout.getParent()).removeView(cblayout);
                                }
                            });
                        }
                    });
                    mTodoListContainer.addView(cblayout);

                }
            }
            mTodoListContainer.measure(0,0);
            if(mTodoListContainer.getMeasuredHeight()>mMaxHeight){
                ViewGroup.LayoutParams params = mTodoListContainer.getLayoutParams();
                params.height = (int) mMaxHeight;
                params.width = MATCH_PARENT;
                mTodoListContainer.setLayoutParams(params);
                mDisplayMore.setVisibility(View.VISIBLE);
            } else {
                ViewGroup.LayoutParams params = mTodoListContainer.getLayoutParams();
                params.height = WRAP_CONTENT;
                params.width = MATCH_PARENT;
                mTodoListContainer.setLayoutParams(params);
                mDisplayMore.setVisibility(View.GONE);
            }

        }

        public void setBackground(String color) {
            int attr = R.attr.NoteBGNone;
            int borderColorAttr = R.attr.NoBackgroundCardBorderColor;
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
            if(attr != R.attr.NoteBGNone)
                borderColorAttr = attr;
            TypedValue typedValue = new TypedValue();

            mTheme.resolveAttribute(attr, typedValue, true);
            @ColorInt int colorInt = typedValue.data;
            if(mCard instanceof CardView)
                ((CardView)mCard).setCardBackgroundColor(colorInt);
            else
                mCard.setBackgroundColor(colorInt);
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


            mTheme.resolveAttribute(borderColorAttr, typedValue, true);
            @ColorInt int borderColorInt = typedValue.data;
            background = mCard.findViewById(R.id.border_layout).getBackground();
            if (background instanceof ShapeDrawable) {
                ShapeDrawable shapeDrawable = (ShapeDrawable) background;
                shapeDrawable.getPaint().setColor(borderColorInt);
            } else if (background instanceof GradientDrawable) {
                // cast to 'GradientDrawable'
                GradientDrawable gradientDrawable = (GradientDrawable) background;
                gradientDrawable.setColor(borderColorInt);
            } else if (background instanceof ColorDrawable) {
                // alpha value may need to be set again after this call
                ColorDrawable colorDrawable = (ColorDrawable) background;
                colorDrawable.setColor(borderColorInt);
            }
        }

        public void setAudioStatus(boolean isPlaying){

        }

        public void setAudioFiles(List<String> names) {

        }



        public void setPreview(ImageView view, Bitmap bitmap, boolean setRound) {
            float pixels= 30;
            if(bitmap == null) {
                view.setVisibility(View.GONE);
                view.setImageBitmap(null);
            }
            else {
                view.setImageBitmap(bitmap);
                if(Build.VERSION.SDK_INT>=22){
                    if(setRound) {
                        view.setOutlineProvider(new ViewOutlineProvider() {
                            public void getOutline(View view, Outline outline) {
                                outline.setRoundRect(0, -(int) pixels, view.getWidth(), view.getHeight(), pixels);
                            }
                        });
                        view.setClipToOutline(true);
                    } else
                        view.setOutlineProvider(null);
                }
                view.setVisibility(View.VISIBLE);

            }


        }

        public void setText(String s) {
            if(s==null)
                s = "";
            if(mNote.mMetadata.urls.size()>0)//otherwise old notes will appear empty
                s = Pattern.compile("(?:(?:https?|ftp|file):\\/\\/|www\\.|ftp\\.)(?:\\([-A-Z0-9+&@#\\/%=~_|$?!:,.]*\\)|[-A-Z0-9+&@#\\/%=~_|$?!:,.])*(?:\\([-A-Z0-9+&@#\\/%=~_|$?!:,.]*\\)|[A-Z0-9+&@#\\/%=~_|$])", Pattern.CASE_INSENSITIVE).matcher(s).replaceAll("");
            
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
        if (viewType == NOTE_GRID) {
            return new NoteViewHolder(LayoutInflater.from(mContext).inflate(R.layout.grid_note_layout, null));
        }
        else if (viewType == NOTE_LINE)
            return new NoteViewHolder(LayoutInflater.from(mContext).inflate(R.layout.line_note_layout, null));

        else return null;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == NOTE_GRID || holder.getItemViewType() == NOTE_LINE) {
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
            if(!note.isFake) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (note.equals(viewHolder.getNote()) && note.needsUpdateInfo) {
                            mNoteInfoRetriever.addNote(note.path);
                        }
                    }
                }, 500);
                if (note.previews.size() > 0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (note.equals(viewHolder.getNote()))
                                mNoteThumbnailEngine.addNote(note, viewHolder);
                        }
                    });
                }
            } else if (note.previews.size() > 0){

                try {
                    Bitmap b2 = BitmapFactory.decodeStream(mContext.getAssets().open(note.previews.get(0)));
                    b2.setDensity(Bitmap.DENSITY_NONE);
                    viewHolder.setPreview(viewHolder.mPreview1, b2, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    public int getItemViewType(int position) {
        if (mNotes.get(position) instanceof Note)
            return mIsInline?NOTE_LINE: NOTE_GRID;
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

        void onAudioClick(Note note, String media);
    }

    @Override
    public int getItemCount() {
        Log.d("notedebug", "mNotes " + mNotes.size());

        return mNotes.size();
    }
}
