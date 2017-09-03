package com.spisoft.quicknote.editor.pages;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.spisoft.quicknote.databases.page.PageManager;

/**
 * Created by alexandre on 18/10/16.
 */

public class PageView extends FrameLayout{

    private RecyclerView mRecyclerView;
    private PageManager mPageManager;
    private PagesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private PagesAdapter.OnPageSelectedListener mOnPageSelectedListener;

    public PageView(Context context) {
        super(context);
        init();
    }

    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mRecyclerView = new RecyclerView(getContext());
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PagesAdapter();
        mRecyclerView.setAdapter(mAdapter);
        addView(mRecyclerView, LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
    }

    public void setPageManager(PageManager pageManager){
        mPageManager = pageManager;
        mAdapter.setPageManager(mPageManager);
    }

    public void setOnPageSelectedListener(PagesAdapter.OnPageSelectedListener OnPageSelectedListener){
        mOnPageSelectedListener = OnPageSelectedListener;
        mAdapter.setOnPageSelectedListener(mOnPageSelectedListener);
    }

    public void notifyDatasetChanged() {
        mAdapter.notifyDataSetChanged();
    }
}
