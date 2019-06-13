package com.spisoft.quicknote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.spisoft.quicknote.browser.PermissionChecker;
import com.spisoft.quicknote.editor.EditorView;
import com.spisoft.quicknote.intro.HelpActivity;
import com.spisoft.quicknote.utils.PinView;
import com.spisoft.sync.account.AccountListActivity;
import com.spisoft.sync.account.DBAccountHelper;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private SharedPreferences.OnSharedPreferenceChangeListener changeListener;
    private PermissionChecker mPermissionChecker;

    public SettingsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);
        findPreference("pref_root_path").setOnPreferenceClickListener(this);
        findPreference("pref_root_path").setSummary(PreferenceHelper.getRootPath(getActivity()));
        findPreference("pref_google_drive").setOnPreferenceClickListener(this);
        findPreference("pref_password_on_minimize").setOnPreferenceChangeListener(this);
        findPreference("pref_set_password").setOnPreferenceClickListener(this);
        findPreference("pref_report_bug").setOnPreferenceClickListener(this);
        findPreference("pref_remove_ad_pay").setOnPreferenceClickListener(this);
        findPreference("pref_paypal").setOnPreferenceClickListener(this);
        findPreference("pref_desktop_version").setOnPreferenceClickListener(this);
        findPreference("pref_changelog").setOnPreferenceClickListener(this);
        ((CheckBoxPreference)findPreference("pref_debug_log")).setChecked(BuildConfig.DEBUG);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d("prefdebug", "key "+key);
                if(key.equals("theme")) {
                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            getActivity().startActivity(intent);
                            Runtime.getRuntime().exit(0);
                        }
                    },1000);

                }
            }
        };
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(changeListener);

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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.beta_feature);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((SettingsActivity)getActivity()).getStoragePermission(new PermissionChecker.PermissionCallback() {
                        @Override
                        public void onPermission(boolean given) {
                            if(given){
                                StorageDialog dialogs = new StorageDialog();
                                dialogs.show(((AppCompatActivity)getActivity()).getSupportFragmentManager(),"" );
                            }
                        }
                    });


                }
            }).setCancelable(true).setNegativeButton(android.R.string.cancel, null).show();
            return true;

        }
        if(preference==findPreference("pref_google_drive")){
            Cursor cursor = DBAccountHelper.getInstance(getActivity()).getCursor();
            if(cursor == null || cursor.getCount() == 0){
                Intent intent = new Intent(getActivity(), HelpActivity.class);
                intent.putExtra(HelpActivity.SYNC_ONLY, true);
                startActivity(intent);
            }
            else
                startActivity(new Intent(getActivity(),AccountListActivity.class));
            return true;
        }else if(preference==findPreference("pref_desktop_version")){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/PhieF/CarnetDocumentation/blob/master/README.md"));
            startActivity(browserIntent);
            return true;
        }else if(preference==findPreference("pref_remove_ad_pay")){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://liberapay.com/~34946"));
            startActivity(browserIntent);
            return true;
        }else if(preference==findPreference("pref_paypal")){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=YMHT55NSCLER6"));
            startActivity(browserIntent);
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
        else if(preference==findPreference("pref_changelog")){
            startActivity(new Intent(getActivity(),ChangelogActivity.class));
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
