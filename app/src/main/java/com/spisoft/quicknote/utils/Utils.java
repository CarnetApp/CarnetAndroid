package com.spisoft.quicknote.utils;

import android.content.Context;

/**
 * Created by phoenamandre on 01/05/16.
 */
public class Utils {

    public static boolean isDebug(Context context){
        if(context.getApplicationContext()!=null)
            return context.getApplicationContext().getPackageName().endsWith(".debug");
        return context.getPackageName().endsWith(".debug");
    }
}
