package com.spisoft.quicknote.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexandre on 13/05/16.
 */
public class FileLocker {

    private static Map<String, Object> sLockedPaths = new HashMap<>();
    public static synchronized Object getLockOnPath(String path){


        boolean contains = false;

        File file = new File(path);
        while(file!=null){
            if(sLockedPaths.containsKey(file.getAbsolutePath())){
                contains = true;
                path = file.getAbsolutePath();
                break;
            }
            file = file.getParentFile();
        }
        if(!contains){
            sLockedPaths.put(path, new Object());

        }
       return sLockedPaths.get(path);
    }

}
