package com.spisoft.quicknote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;

import com.spisoft.quicknote.utils.PinView;

/**
 * Created by phoenamandre on 07/02/16.
 */
public class PasswordDialog extends DialogFragment implements PinView.PasswordListener {


    boolean mHasUnlock = false;
    private PinView mPinView;
    private Handler handler = new Handler(){};
    private PinView.PasswordListener mListener;

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mHasUnlock = !PreferenceHelper.shouldLockOnMinimize(getActivity());

        mPinView = new PinView(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mPinView.setPasswordListener(this);
        builder.setView(mPinView);
        setState();

        return builder.create();
    }

    private void setState() {
        if(mHasUnlock){
            mPinView.setTitle(getString(R.string.new_password));
        }
        else
            mPinView.setTitle(getString(R.string.old_password));
    }

    public void setPasswordListener(PinView.PasswordListener listener){
        mListener = listener;
    }
    @Override
    public boolean checkPassword(String password) {
        if(mHasUnlock){
            PreferenceHelper.setPassword(getActivity(), password);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            });

            return true;
        }
        else if(PreferenceHelper.getPassword(getActivity()).equals(password)) {
            mHasUnlock = true;

            return true;
        }

        else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void onPasswordOk() {
        mPinView.clear();
        setState();
        if(mListener!=null)
            mListener.onPasswordOk();
    }
}
