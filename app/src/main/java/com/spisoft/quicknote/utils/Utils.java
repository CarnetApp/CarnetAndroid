package com.spisoft.quicknote.utils;

import android.content.Context;
import android.util.DisplayMetrics;

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
}
