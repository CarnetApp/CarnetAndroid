package com.spisoft.quicknote.synchro;

import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by alexandre on 18/11/16.
 */

public class WrapperFactory {

    public Wrapper getWrapper(Context ct, int accountType, int accountID) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        for(Class wrapperClass : AccountManager.wrappers){
            Method m = wrapperClass.getMethod("isMyAccount", Double.class, String.class);
            boolean result = (boolean) m.invoke(null, accountType);
            if(result){
               return(Wrapper) wrapperClass.getConstructor(Context.class, Integer.class).newInstance(ct, accountID);
            }
        }
        return null;
    }
}
