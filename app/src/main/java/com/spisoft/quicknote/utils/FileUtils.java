package com.spisoft.quicknote.utils;

import android.content.Context;
import android.net.Uri;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.databases.RecentHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by alexandre on 09/02/16.
 */
public class FileUtils {
    public static  String getExtension(String filename) {
        if (filename == null)
            return null;
        int dotPos = filename.lastIndexOf('.');
        if (dotPos >= 0 && dotPos < filename.length()) {
            return filename.substring(dotPos + 1).toLowerCase();
        }
        return null;
    }

    public static String getNameWithoutExtension(String path){
        String name = Uri.parse(path).getLastPathSegment();
        name = name.substring(0, name.length()-getExtension(name).length());
        return name;
    }

    public static boolean deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        return fileOrDirectory.delete();

    }

    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len);

        }
        inputStream.close();
        outputStream.close();

    }

    public static void copy(RandomAccessFile inputStream, OutputStream outputStream) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len);

        }
        inputStream.close();
        outputStream.close();

    }
    public static String renameDirectory(Context context,File directory, String newName){
        File notFile = directory;
        File toFile = new File(notFile.getParentFile(), newName);
        synchronized (FileLocker.getLockOnPath(notFile.getAbsolutePath())) {
            synchronized (FileLocker.getLockOnPath(toFile.getAbsolutePath())) {
                if (!toFile.exists()) {

                    FileUtils.moveDirectoryOneLocationToAnotherLocation(notFile, toFile);
                    RecentHelper.getInstance(context).renameDirectory(directory.getAbsolutePath(), toFile.getAbsolutePath());

                    return toFile.getAbsolutePath();

                }
            }
        }
        return null;
    }

    public static boolean moveDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation)
    {
        if(targetLocation.getAbsolutePath().contains(sourceLocation.getAbsolutePath()+"/"))
            return false;
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                if(!targetLocation.mkdir())
                    return false;
            }
            File[] children = sourceLocation.listFiles();
            for (int i = 0; i < children.length; i++) {
                if(!moveDirectoryOneLocationToAnotherLocation(children[i],
                        new File(targetLocation, children[i].getName())))
                    return false;
            }
            return sourceLocation.delete();
        } else {
            targetLocation.getParentFile().mkdirs();
            if(!targetLocation.exists()) {
                if(sourceLocation.renameTo(targetLocation)){
                    if (sourceLocation.getAbsolutePath().endsWith(".sqd"))
                        RecentHelper.getInstance(null).moveNote(new Note(sourceLocation.getAbsolutePath()), targetLocation.getAbsolutePath());
                    return true;
                }

                else return false;
            }
            else return false;
        }


    }

    public static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation)
    {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            File[] children = sourceLocation.listFiles();
            for (int i = 0; i < children.length; i++) {

                copyDirectoryOneLocationToAnotherLocation(children[i],
                        new File(targetLocation, children[i].getName()));
            }
        } else {
            targetLocation.getParentFile().mkdirs();
            try {
                copy(new FileInputStream(sourceLocation), new FileOutputStream(targetLocation));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public static String getMD5(File f) throws NoSuchAlgorithmException, IOException{

        MessageDigest md = MessageDigest.getInstance("MD5");

        int byteArraySize = 2048;



        InputStream is = new FileInputStream(f);
        md.reset();
        byte[] bytes = new byte[byteArraySize];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
            md.update(bytes, 0, numBytes);
        }
        byte[] digest = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
    public static String md5(String tmpDownloadPath) {
        try {
            return getMD5(new File(tmpDownloadPath));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
