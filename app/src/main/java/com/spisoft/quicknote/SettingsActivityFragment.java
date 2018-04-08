package com.spisoft.quicknote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import com.spisoft.quicknote.billingutils.BillingUtils;
import com.spisoft.quicknote.billingutils.IsPaidCallback;
import com.spisoft.quicknote.intro.HelpActivity;
import com.spisoft.quicknote.synchro.googledrive.AuthorizeActivity;
import com.spisoft.quicknote.utils.PinView;
import com.spisoft.sync.account.AccountListActivity;
import com.spisoft.sync.account.DBAccountHelper;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private BillingUtils u;
    private IsPaidCallback isPaidCallback;

    public SettingsActivityFragment() {
    }

    public void launchPurchase() {
        // TODO Auto-generated method stub
        u.purchase(getActivity(), isPaidCallback);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        u = new BillingUtils(getActivity());
        isPaidCallback =new IsPaidCallback(getActivity()) {

            @Override
            public void hasBeenPaid(int isPaid) {
                super.hasBeenPaid(isPaid);
                if(!isAdded())
                    return;
                if (checkPayement(isPaid)) {
                    setPaidStatus();
                } else {
                    setFreeStatus();
                }
            }
        };
        addPreferencesFromResource(R.xml.pref_general);
        findPreference("pref_root_path").setOnPreferenceClickListener(this);
        findPreference("pref_google_drive").setOnPreferenceClickListener(this);
        findPreference("pref_password_on_minimize").setOnPreferenceChangeListener(this);
        findPreference("pref_set_password").setOnPreferenceClickListener(this);
        findPreference("pref_report_bug").setOnPreferenceClickListener(this);
        findPreference("pref_remove_ad_pay").setOnPreferenceClickListener(this);
        u.checkPayement(isPaidCallback);

    }

    private void setFreeStatus() {

    }

    private void setPaidStatus() {
        getPreferenceScreen().removePreference(findPreference("paiement_header"));
    //    getPreferenceScreen().removePreference(findPreference("pref_remove_ad_pay"));
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        if(preference==findPreference("pref_root_path")){
            StorageDialog dialog = new StorageDialog();
            dialog.show(((AppCompatActivity)getActivity()).getSupportFragmentManager(),"" );
            return true;
        }
        if(preference==findPreference("pref_google_drive")){
            Cursor cursor = DBAccountHelper.getInstance(getActivity()).getCursor();
            if(cursor == null || cursor.getCount() == 0){
                startActivity(new Intent(getActivity(), HelpActivity.class));
            }
            else
                startActivity(new Intent(getActivity(),AccountListActivity.class));
            return true;
        }else if(preference==findPreference("pref_set_password")){
            PasswordDialog dialog = new PasswordDialog();
            dialog.show(((AppCompatActivity)getActivity()).getSupportFragmentManager(),"" );
            return true;
        }
        else if(preference==findPreference("pref_report_bug")){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"spipinoza@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "About Quicknote");

            startActivity(Intent.createChooser(intent, "Send Email"));
            return true;
        }
        else
        return true;
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, Object o) {
        if(preference==findPreference("pref_password_on_minimize")){
            if(PreferenceHelper.shouldLockOnMinimize(getActivity())) {
                final PasswordDialog dialog = new PasswordDialog();
                dialog.setPasswordListener(new PinView.PasswordListener() {
                    @Override
                    public boolean checkPassword(String password) {
                        //will be checked by dialog itself
                        return false;
                    }

                    @Override
                    public void onPasswordOk() {
                        PreferenceHelper.setShouldAskPasswordOnMinimize(getActivity(), false);
                        ((CheckBoxPreference)findPreference("pref_password_on_minimize")).setChecked(PreferenceHelper.shouldLockOnMinimize(getActivity()));
                        dialog.dismiss();
                    }
                });
                dialog.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), "");
                return false;
            }
            return true;
        }
        
        return true;
    }
}
