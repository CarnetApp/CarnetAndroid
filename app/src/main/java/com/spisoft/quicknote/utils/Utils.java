package com.spisoft.quicknote.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.spisoft.quicknote.R;

import java.util.List;

/**
 * Created by phoenamandre on 01/05/16.
 */
public class Utils {

    public static boolean isDebug(Context context){
        if(context.getApplicationContext()!=null)
            return context.getApplicationContext().getPackageName().endsWith(".debug");
        return context.getPackageName().endsWith(".debug");
    }
    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    public static boolean isMainLiss(Context context){
        if(context == null)
            return true;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities( mainIntent, 0);
        for(ResolveInfo info : pkgAppsList){
            if(info.activityInfo.packageName!=null&&info.activityInfo.packageName.contains("lissl")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGoogle(Context context) {
        return context.getResources().getBoolean(R.bool.is_google);
    }

    public static String getCurrentTheme(Context mContext) {
        String theme = PreferenceManager.getDefaultSharedPreferences(mContext).getString("theme","auto");
        if(theme.equals("auto")) {
            int nightModeFlags =
                    mContext.getResources().getConfiguration().uiMode &
                            android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case android.content.res.Configuration.UI_MODE_NIGHT_YES:
                    theme = "black";
                    break;

                case android.content.res.Configuration.UI_MODE_NIGHT_NO:
                    theme = "carnet";
                    break;
            }
        }
        return theme;
    }
}
