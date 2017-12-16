package com.spisoft.quicknote.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.page.PageManager;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileLock;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by alexandre on 29/03/16.
 */
public class ZipUtils {

    public static SerialExecutor executor = new SerialExecutor();
    public static boolean addEntry(Context ct,Note note,String path, Uri file){
        synchronized (FileLocker.getLockOnPath(file.toString())) {
            FileLock lock = null;
            if (path.endsWith("/") && file != null) {//file
                path += path + file.getLastPathSegment();
            }
            try {
                return addEntry(ct, note, path, new FileInputStream(file.toString()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return false;
        }

    }

    public static String getTmpPath(Context ct){
        return NoteManager.getDontTouchFolder(ct)+"/.tmpsavenote";
    }

    public static boolean deleteEntry(Context ct,Note note,String path){
        boolean ret = false;
        synchronized (FileLocker.getLockOnPath(note.path)) {
            FileLock lock = null;

            if (path.startsWith("/"))
                path = path.substring(1);
            String tmp = getTmpPath(ct);
            try {


                FileOutputStream fos = new FileOutputStream(tmp);
                lock = fos.getChannel().lock();
                ZipOutputStream zos = new ZipOutputStream(fos);
                byte[] buf = new byte[1024];

                Log.d("zipdebug", "addEntry1");
                if (new File(note.path).exists()) {
                    ZipInputStream zin = new ZipInputStream(new FileInputStream(note.path));
                    Log.d("zipdebug", "addEntry" + tmp);
                    copyToZip(zin, zos, path);
                }
                Log.d("zipdebug", "addEntry2");


                zos.close();
                File noteFile = new File(note.path);
                noteFile.delete();
                new File(tmp).renameTo(noteFile);
                ret = true;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("TestDebug", "write error");

                new File(tmp).delete();
            } finally {
                if (lock != null && lock.isValid())
                    try {
                        lock.release();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        return ret;

    }

    public static boolean addEntry(Context ct,Note note,String path, InputStream stream){
        boolean ret = false;
        synchronized (FileLocker.getLockOnPath(note.path)) {
            FileLock lock = null;

            if (path.startsWith("/"))
                path = path.substring(1);
            String tmp = getTmpPath(ct);
            new File(tmp).getParentFile().mkdirs(); //parent dirs
            try {


                FileOutputStream fos = new FileOutputStream(tmp);
                ZipOutputStream zos = new ZipOutputStream(fos);
                byte[] buf = new byte[1024];
                lock = fos.getChannel().lock();
                Log.d("zipdebug", "addEntry1" + tmp);
                if (new File(note.path).exists()) {
                    ZipInputStream zin = new ZipInputStream(new FileInputStream(note.path));
                    Log.d("zipdebug", "addEntry" + tmp);
                    copyToZip(zin, zos, path);
                }
                Log.d("zipdebug", "addEntry2");

                InputStream in = stream;
                // Add ZIP entry to output stream.
                zos.putNextEntry(new ZipEntry(path));
                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                Log.d("zipdebug", "addEntry3");

                // Complete the entry
                zos.closeEntry();
                in.close();
                zos.close();
                File noteFile = new File(note.path);
                noteFile.delete();
                new File(tmp).renameTo(noteFile);
                ret = true;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("TestDebug", "write error");

                new File(tmp).delete();
            } finally {
                if (lock != null && lock.isValid())
                    try {
                        lock.release();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        return ret;

    }

    public static boolean zipFolder(File folder,String path){
        synchronized (FileLocker.getLockOnPath(folder.getAbsolutePath())) {
            synchronized (FileLocker.getLockOnPath(path)) {
                FileLock lock = null;
                boolean ret = false;
                String tmp = path;
                try {

                    FileOutputStream fos = new FileOutputStream(tmp);
                    ZipOutputStream zos = new ZipOutputStream(fos);
                   // zos.setMethod(ZipOutputStream.DEFLATED); // this line optional
                    zos.setLevel(0);
                    lock = fos.getChannel().lock();

                    recursiveAddFile(folder, zos, folder.getAbsolutePath());


                    // Complete the entry

                    zos.close();
                    ret = true;
                } catch (IOException e) {
                    Log.d("TestDebug", "write error");
                    e.printStackTrace();
                    new File(tmp).delete();
                } finally {
                    if (lock != null && lock.isValid())
                        try {
                            lock.release();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }

                return ret;
            }
        }

    }

    private static void recursiveAddFile(File file, ZipOutputStream zos, String rootFolderPathWithoutSlash) throws IOException {
        byte[] buf = new byte[1024];
        if(file.isDirectory()){
            File [] files = file.listFiles();
            if(!file.getAbsolutePath().equals(rootFolderPathWithoutSlash))
            zos.putNextEntry(new ZipEntry(file.getAbsolutePath().substring(rootFolderPathWithoutSlash.length()+1)));
            if(files!=null)
                for (File child : files)
                    recursiveAddFile(child,zos, rootFolderPathWithoutSlash);
        }else {
            // Add ZIP entry to output stream.
            ZipEntry entry = new ZipEntry(file.getAbsolutePath().substring(rootFolderPathWithoutSlash.length()+1));
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(file.length());
            entry.setCompressedSize(file.length());
            Checksum checksum = new CRC32();
            final byte[] content = new byte[(int) file.length()];
            InputStream in = new FileInputStream(file);
            for (int off = 0, read;
                 (read = in.read(content, off, content.length - off)) > 0;
                 off += read)
                ;
            checksum.update(content, 0, content.length);
            entry.setCrc(checksum.getValue());
            zos.putNextEntry(entry);
            // Transfer bytes from the file to the ZIP file
            Log.d("zipdebug", "addEntry "+file.getAbsolutePath());

            zos.write(content, 0, content.length);

            in.close();
            zos.closeEntry();
        }

    }


    public static void copyToZip(ZipInputStream zin, ZipOutputStream zos, String skip) throws IOException {
        byte[] buf = new byte[1024];
        ZipEntry entry = zin.getNextEntry();
        Log.d("zipdebug", "skip" + skip);
        while (entry != null) {
            if(!entry.getName().equals(skip)) {
                Log.d("zipdebug", "name" + entry.getName());
                zos.putNextEntry(entry);
                // Transfer bytes from the ZIP file to the output file
                int len;
                while ((len = zin.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
            }
            entry = zin.getNextEntry();
        }
    }
    public interface WriterListener{
        public void onError();
    }

    public static void savePageManager(final Context ct, final Note note, final PageManager pageManager, final WriterListener writerListener){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                Log.d("zipdebug", "on change page manager");

                // convert String into InputStream
                InputStream is = new ByteArrayInputStream(pageManager.toJson().getBytes());
                if(!addEntry(ct,note,note.PAGE_INDEX_PATH, is))
                    writerListener.onError();
                return null;
            }
        }.executeOnExecutor(executor);

    }


    public static void changeText(final Context ct, final Note note, final String path, final String txt, final WriterListener writerListener){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                // convert String into InputStream
                InputStream is = new ByteArrayInputStream(txt.getBytes());
                if(!addEntry(ct,note,path, is))
                    writerListener.onError();
                return null;
            }
        }.executeOnExecutor(executor);

    }

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                Log.d("zipdebug",entry.getName()+" is a file ");
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        File parent = new File(filePath).getParentFile();
        if(parent.exists() && !parent.isDirectory())
            parent.delete();
        parent.mkdirs();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[1024];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}
