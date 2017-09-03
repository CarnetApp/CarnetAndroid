package com.spisoft.quicknote.editor;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spisoft.quicknote.R;

/**
 * Created by alexandre on 12/02/16.
 */
public class BubbleLayout extends LinearLayout {

    private final ViewGroup mMainContainer;
    private final ViewGroup mSecondaryContainer;
    private final boolean mIsLoaded;
    private final TextView mToastTv;
    private View mMainView;
    private Handler mHandler = new Handler(){

        public void handleMessage(Message msg) {
            mListener.onRemoveToast();
            mToastTv.setVisibility(GONE);
        }
    };

    private OnBubbleClickListener mListener;

    public View getMainView() {
        return mMainView;
    }


    public interface OnBubbleClickListener{
        public void onMainBubbleClick();
        public void onSecondaryBubbleClick(View v);
        void onRemoveToast();
        void onDisplayToast();
    }

    public BubbleLayout(Context context) {
        this(context, null);
    }


    public BubbleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.bubble_layout, this);

        mMainContainer = (ViewGroup)findViewById(R.id.main_container);
        mToastTv = (TextView)findViewById(R.id.toast_tv);
        mSecondaryContainer =(ViewGroup) findViewById(R.id.secondary_container);
        mIsLoaded = true;


    }

    public void addView(View child, int index, LayoutParams params) {

        if(!mIsLoaded)
            super.addView(child,index,params);
        else{

            if(mMainView==null)
                setMainBubbleView(child);
            else
                addSecondaryBubbleView(child, params);
        }
    }
    public void setMainBubbleView(View v){
        mMainView = v;
        mMainContainer.removeAllViews();
        mMainContainer.addView(v);
    }

    public void makeToast(String txt){
        mToastTv.setText(txt);
        mToastTv.setVisibility(VISIBLE);
        mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 3000);
        mListener.onDisplayToast();
    }
    public void addSecondaryBubbleView(final View v, ViewGroup.LayoutParams params){
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSecondaryBubbleClick(v);
            }
        });
        mSecondaryContainer.addView(v,  params);

    }
    public void setOnBubbleClickListener(OnBubbleClickListener listener){
        mListener= listener;
    }
    public void onMainClick() {
        mListener.onMainBubbleClick();

    }

}
