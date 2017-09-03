package com.spisoft.quicknote.synchro;

import com.spisoft.quicknote.synchro.googledrive.DriveWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 18/11/16.
 */

public class AccountManager {

    public static final List<Class> wrappers = new ArrayList<Class>(){{
        add(DriveWrapper.class);
    }};


}
