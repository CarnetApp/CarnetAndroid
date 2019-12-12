package com.spisoft.quicknote.browser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.spisoft.quicknote.R;

/**
 * Created by phoenamandre on 14/02/16.
 */
public class RenameDialog extends DialogFragment implements View.OnClickListener {
    private EditText mTextView;
    private String mName;
    private OnRenameListener mRenameListener;
    private String mHint;

    @Override
    public void onClick(View v) {
        if(mRenameListener.renameTo(mTextView.getText().toString()))
            dismiss();

    }

    public interface OnRenameListener{
        public boolean renameTo(String name);
    }
    public void setRenameListener(OnRenameListener renameListener){
        mRenameListener = renameListener;
    }
    public void setName(String name){
        mName = name;
    }
    public void setHint(String hint){
        mHint = hint;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.name);
        mTextView = new EditText(getActivity());
        if(mName!=null)
            mTextView.setText(mName);
        mTextView.setHint(mHint);
        builder.setView(mTextView);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        final AlertDialog d = builder.create();
        d.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(RenameDialog.this);
            }
        });
        return d;
    }
}
