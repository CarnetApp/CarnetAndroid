package com.spisoft.quicknote.synchro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;

import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.sync.Log;
import com.spisoft.sync.account.DBAccountHelper;
import com.spisoft.sync.browsing.FilePickerActivity;
import com.spisoft.sync.wrappers.Wrapper;

/**
 * A placeholder fragment containing a simple view.
 */
public class AccountConfigActivityFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private static final int REQUEST_FILE_PICK = 1001;
    private static final String TAG = "AccountConfigActivityFragment";
    private Preference mBrowsePreference;
    private int mAccountId;
    private int mAccountType;
    private Wrapper mWrapper;
    private String mCurrentlySetPath;
    private Preference mChangeCredentialsPreference;
    private Preference mDeleteAccountPreference;

    public AccountConfigActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.empty_pref);
        mAccountId =  getActivity().getIntent().getIntExtra(AccountConfigActivity.EXTRA_ACCOUNT_ID,-1);
        Log.d("accounddebug","config "+mAccountId);
        mAccountType =  getActivity().getIntent().getIntExtra(AccountConfigActivity.EXTRA_ACCOUNT_TYPE,-1);
        mWrapper = com.spisoft.sync.wrappers.WrapperFactory.getWrapper(getActivity(),mAccountType, mAccountId);
        mCurrentlySetPath = mWrapper.getRemoteSyncDir(PreferenceHelper.getRootPath(getActivity()));
        mBrowsePreference = new Preference(getActivity());
        mBrowsePreference.setTitle(R.string.remote_folder);
        mBrowsePreference.setSummary(mCurrentlySetPath);
        mBrowsePreference.setOnPreferenceClickListener(this);
        getPreferenceScreen().addPreference(mBrowsePreference);
        if(mWrapper.canChangeCredentials()){
            mChangeCredentialsPreference = new Preference(getActivity());
            mChangeCredentialsPreference.setTitle(R.string.change_credentials);
            mChangeCredentialsPreference.setOnPreferenceClickListener(this);
            getPreferenceScreen().addPreference(mChangeCredentialsPreference);
        }
        mDeleteAccountPreference = new Preference(getActivity());
        mDeleteAccountPreference.setTitle(R.string.delete);
        mDeleteAccountPreference.setOnPreferenceClickListener(this);
        getPreferenceScreen().addPreference(mDeleteAccountPreference);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_FILE_PICK){
            if(resultCode== Activity.RESULT_OK) {
                String path = data.getStringExtra(FilePickerActivity.RESULT_PICKER_PATH);
                Log.d(TAG,"New remote path "+path);

                mWrapper.addFolderSync(PreferenceHelper.getRootPath(getActivity()), path);
            }
        }
        else super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mBrowsePreference) {
            Intent intent = new Intent(getActivity(), FilePickerActivity.class);
            intent.putExtra(FilePickerActivity.EXTRA_ACCOUNT_ID, mAccountId);
            intent.putExtra(FilePickerActivity.EXTRA_START_PATH, mCurrentlySetPath);
            intent.putExtra(FilePickerActivity.EXTRA_AS_FILE_PICKER, true);
            intent.putExtra(FilePickerActivity.EXTRA_DISPLAY_ONLY_MIMETYPE, "DIR");
            startActivityForResult(intent, REQUEST_FILE_PICK);
        } else if (preference == mChangeCredentialsPreference){
            mWrapper.startAuthorizeActivityForResult(getActivity(), 0);
        } else if (preference == mDeleteAccountPreference){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.delete_confirm);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mWrapper.deleteAccount();
                    getActivity().finish();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }
        return true;
    }
}
