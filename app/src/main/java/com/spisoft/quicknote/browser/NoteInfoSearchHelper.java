package com.spisoft.quicknote.browser;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.server.ZipReaderAndHttpProxy;

import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by phiedora on 26/07/18.
 */

public class NoteInfoSearchHelper {
    public NoteInfoSearchHelper(Context context) {

    }


    protected Pair<String, Boolean> readZipEntry(ZipFile zp, ZipEntry entry, long length, int maxLines, String toFind){
        String sb = new String();
        BufferedReader br = null;

        boolean hasFound = toFind == null;
        if(toFind!=null)
            toFind = toFind.toLowerCase();
        try {
            br = new BufferedReader(  br = new BufferedReader(new InputStreamReader(zp.getInputStream(entry))));

            String line = br.readLine();
            long total=0;
            int lines = 0;
            maxLines= 352623523;
            while (line != null) {
                if((total<length||length==-1)&&(lines==-1||lines<maxLines)) {
                    sb += line;
                    sb += "\n";
                }
                total = Jsoup.parse(sb).text().length();
                if(!hasFound){
                    if(line.toLowerCase().contains(toFind)){
                        hasFound = true;
                    }
                }
                line = null;
                lines++;
                if((total<length||length==-1)&&(lines==-1||lines<maxLines)||!hasFound)
                    line = br.readLine();

            }
            sb = Jsoup.parse(sb).text();
            if(!hasFound){
                if(sb.toLowerCase().contains(toFind)){
                    hasFound = true;
                }
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(br!=null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return new Pair<>(sb.toString(), hasFound);
    }

    protected Pair<String, Boolean> read(ZipFile zp, ZipEntry entry, long length, int maxLines, String toFind){
        return readZipEntry(zp, entry,length, maxLines, toFind);
    }

}