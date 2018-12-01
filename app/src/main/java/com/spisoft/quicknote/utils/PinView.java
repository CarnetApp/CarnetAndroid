package com.spisoft.quicknote.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.spisoft.quicknote.FloatingFragment;
import com.spisoft.quicknote.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 19/02/16.
 */
public class PinView extends FrameLayout implements View.OnClickListener, FloatingFragment {
    List<View> buttons = new ArrayList<>();
    List<TextView> textViews = new ArrayList<>();
    private String mCurrentPassword;
    private AsyncTask mPasswordCheckTask;
    private boolean mIsChecking;

    public PinView(Context context) {
        super(context);
        init();
    }

    public PinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PinView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    PasswordListener mPasswordListener;
    public interface PasswordListener{
        boolean checkPassword(String password);
        void onPasswordOk();
    }
    private void init() {
        mCurrentPassword="";
        mIsChecking = false;
        LayoutInflater.from(getContext()).inflate(R.layout.pin_layout, this);
        buttons.add(findViewById(R.id.button_0));
        buttons.add(findViewById(R.id.button_1));
        buttons.add(findViewById(R.id.button_2));
        buttons.add(findViewById(R.id.button_3));
        buttons.add(findViewById(R.id.button_4));
        buttons.add(findViewById(R.id.button_5));
        buttons.add(findViewById(R.id.button_6));
        buttons.add(findViewById(R.id.button_7));
        buttons.add(findViewById(R.id.button_8));
        buttons.add(findViewById(R.id.button_9));

        for(View v: buttons)
            v.setOnClickListener(this);

        textViews.add((TextView) findViewById(R.id.text1));
        textViews.add((TextView) findViewById(R.id.text2));
        textViews.add((TextView) findViewById(R.id.text3));
        textViews.add((TextView) findViewById(R.id.text4));

        findViewById(R.id.button_back).setOnClickListener(this);
        clear();

    }

    @Override
    public void onClick(View view) {
        if(mIsChecking)
            return;
        int index = buttons.indexOf(view);
        if(index!=-1){
            addNum(index);
        }else{
            if (view==findViewById(R.id.button_back)){
                goBack();
            }
        }
    }

    private void addNum(int index) {
        if(mCurrentPassword.length()<4){
            mCurrentPassword+=index;
            textViews.get(mCurrentPassword.length()-1).setText("X");
        }if(mCurrentPassword.length()==4) {
            mPasswordCheckTask = new AsyncTask<String, Void, Boolean>() {
                protected void onPreExecute() {
                    findViewById(R.id.wrong_password).setVisibility(VISIBLE);
                    ((TextView)findViewById(R.id.wrong_password)).setText(R.string.checking_password);
                    mIsChecking = true;

                }

                protected void onPostExecute(Boolean result) {
                    if(!result) {
                        findViewById(R.id.wrong_password).setVisibility(VISIBLE);
                        ((TextView)findViewById(R.id.wrong_password)).setText(R.string.wrong_password);
                    }
                    else mPasswordListener.onPasswordOk();
                    mIsChecking = false;
                    clear();
                }

                @Override
                protected Boolean doInBackground(String... strings) {
                    return mPasswordListener.checkPassword(strings[0]);
                }
            }.execute(mCurrentPassword);

        }

    }


    public void setPasswordListener(PasswordListener listener){
        mPasswordListener = listener;
    }
    public void setTitle(String title){
        ((TextView)findViewById(R.id.textView1)).setText(title);
    }
    private void goBack(){
        if(mCurrentPassword.length()>0){
            textViews.get(mCurrentPassword.length()-1).setText("");
            mCurrentPassword=mCurrentPassword.substring(0, mCurrentPassword.length()-1);

        }
    }
    public void clear(){
        for(TextView v: textViews)
            v.setText("");
        mCurrentPassword="";
    }

    @Override
    public View getView() {
        return this;
    }

}
