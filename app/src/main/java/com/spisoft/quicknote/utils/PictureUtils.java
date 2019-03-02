package com.spisoft.quicknote.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 18/10/16.
 */

public class PictureUtils {
    public static void resize(String input, String output, int maxWidth, int maxHeight) throws IOException {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap photo = BitmapFactory.decodeFile(input);
        int width = photo.getWidth();
        int height = photo.getHeight();


        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int)(width / ratio);
        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }
        photo = Bitmap.createScaledBitmap(photo, width, height, false);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        File f = new File(output);
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
    }

    public static boolean isPicture(String name) {
        String ext = FileUtils.getExtension(name);
        if(ext == null)
            return false;
        List<String> exts = new ArrayList<>();
        exts.add("png");
        exts.add("jpg");
        exts.add("bmp");
        exts.add("gif");
        exts.add("jpeg");

        return exts.contains(ext.toLowerCase());
    }
}
