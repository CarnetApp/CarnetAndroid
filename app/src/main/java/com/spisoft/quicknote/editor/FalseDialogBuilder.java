package com.spisoft.quicknote.editor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.spisoft.quicknote.R;

/**
 * Created by alexandre on 18/02/16.
 */
public class FalseDialogBuilder  {

    private final Context mContext;
    private final View mView;
    private final Button mPositiveButton;
    private final TextView mTextView;
    private final Button mNegativeButton;

    public FalseDialogBuilder(Context context) {
        mContext = context;

        mView = LayoutInflater.from(context).inflate(R.layout.false_dialog_layout, null);
        mTextView =(TextView) mView.findViewById(R.id.text_view);
        mPositiveButton =(Button) mView.findViewById(R.id.positive_button);
        mNegativeButton = (Button) mView.findViewById(R.id.negative_button);
    }

    public void setView(View v){

    }
    public View getView(){
        return mView;
    }
    public void setText(String text){
        mTextView.setText(text);

    }
    public void setOnPositiveButton(String text,View.OnClickListener listener) {
        mPositiveButton.setText(text);
        mPositiveButton.setOnClickListener(listener);
    }
    public void setOnCancelButton(String text,View.OnClickListener listener){
        mNegativeButton.setText(text);
        mNegativeButton.setOnClickListener(listener);

    }
}
