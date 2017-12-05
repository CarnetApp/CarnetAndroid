package com.spisoft.quicknote.synchro;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.sync.wrappers.*;
import com.spisoft.sync.wrappers.Wrapper;

/**
 * A placeholder fragment containing a simple view.
 */
public class AccountConfigActivityFragment extends PreferenceFragment {

    private Preference mBrowsePreference;
    private long mAccountId;
    private int mAccountType;
    private Wrapper mWrapper;

    public AccountConfigActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.empty_pref);
        mAccountId =  getActivity().getIntent().getLongExtra(AccountConfigActivity.EXTRA_ACCOUNT_ID,-1);
        mAccountType =  getActivity().getIntent().getIntExtra(AccountConfigActivity.EXTRA_ACCOUNT_TYPE,-1);
        mWrapper = com.spisoft.sync.wrappers.WrapperFactory.getWrapper(getActivity(),mAccountType, mAccountId);
        mBrowsePreference = new Preference(getActivity());
        mBrowsePreference.setTitle(R.string.remote_folder);
        mBrowsePreference.setSummary(mWrapper.getRemoteSyncDir(PreferenceHelper.getRootPath(getActivity())));
        getPreferenceScreen().addPreference(mBrowsePreference);


    }
}
