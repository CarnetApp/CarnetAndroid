package com.spisoft.quicknote.editor.pages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.spisoft.quicknote.R;
import com.spisoft.quicknote.databases.page.Page;
import com.spisoft.quicknote.databases.page.PageManager;
import com.spisoft.quicknote.utils.FileUtils;

/**
 * Created by alexandre on 18/10/16.
 */

public class PagesAdapter extends RecyclerView.Adapter {

    private PageManager mPageManager;
    private OnPageSelectedListener mOnPageSelectedListener;

    public interface OnPageSelectedListener{
        void onPageSelected(Page page);
    }
    public PagesAdapter() {
    }

    public void setPageManager(PageManager pageManager) {
        this.mPageManager = pageManager;
    }

    public class MyRecyclerView extends RecyclerView.ViewHolder{

        private final ImageView mImageView;
        private final TextView mTitle;
        private Page mPage;

        public MyRecyclerView(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.image_view);
            mTitle = (TextView) itemView.findViewById(R.id.text_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnPageSelectedListener.onPageSelected(mPage);
                }
            });
        }

        public void setPage(Page page){
            mPage=page;
        }
        public void setTitle(String title){
            mTitle.setText(title);
        }

        public void setImage(Bitmap image){
            mImageView.setImageBitmap(image);
        }

        public void setImageRes(int imageRes){
            mImageView.setImageResource(imageRes);
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_item_layout,null);
        return new MyRecyclerView(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Page page = mPageManager.getPage(position);
        if(page.thumbnail!=null) {
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inPreferredConfig = Bitmap.Config.ARGB_8888;
            ((MyRecyclerView) holder).setImage(BitmapFactory.decodeFile(page.thumbnail));
        }
        else
            ((MyRecyclerView) holder).setImageRes(R.drawable.ic_launcher);
        ((MyRecyclerView) holder).setPage(page);
        ((MyRecyclerView) holder).setTitle(FileUtils.getNameWithoutExtension(page.relativePath));
    }

    @Override
    public int getItemCount() {
        return mPageManager!=null?mPageManager.getPageList().size():0;
    }

    public void setOnPageSelectedListener(OnPageSelectedListener OnPageSelectedListener){
        mOnPageSelectedListener = OnPageSelectedListener;
    }
}
