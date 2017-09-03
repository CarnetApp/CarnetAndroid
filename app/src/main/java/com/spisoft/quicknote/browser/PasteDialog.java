package com.spisoft.quicknote.browser;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.spisoft.quicknote.R;

/**
 * Created by alexandre on 12/05/16.
 */
public class PasteDialog extends AlertDialog {
    private final Context mContext;

    public PasteDialog(Context context) {
        super(context);
        mContext = context;
        setTitle(R.string.file_operation_in_progress);
        setMessage(mContext.getResources().getString(R.string.please_wait));
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }
}
