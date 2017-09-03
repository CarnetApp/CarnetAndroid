package com.spisoft.quicknote.serviceactivities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.soundcloud.android.crop.Crop;


public class CropWrapperActivity extends AppCompatActivity {
    private static final String START_PICKER = "picker";
    private static final String START_CROP = "crop";
    private static final String INPUT_URI = "input";
    private static final String OUTPUT_URI = "output";
    private static final String START_COLOR_PICKER = "color_picker";
    private static CroperResultListener slistener;

    public static void startCroper(Context context, CroperResultListener listener, Uri input, Uri output) {
        slistener = listener;
        Intent intent = new Intent(context, CropWrapperActivity.class);
        intent.putExtra(START_CROP, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        intent.putExtra(INPUT_URI, input);
        intent.putExtra(OUTPUT_URI, output);
        context.startActivity(intent);

    }

    public interface CroperResultListener{
        public void onActivityResult(int requestCode, int resultCode, Intent data);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(START_PICKER, false)) {

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, Crop.REQUEST_PICK);
        } else if (getIntent().getBooleanExtra(START_CROP, false)) {
            Log.d("screendebug", "cropping " + ((Uri) getIntent().getParcelableExtra(INPUT_URI)).toString());
            Crop crop = Crop.of((Uri) getIntent().getParcelableExtra(INPUT_URI), (Uri) getIntent().getParcelableExtra(OUTPUT_URI));
            crop.start(this);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        slistener.onActivityResult(requestCode,resultCode,data);
        finish();
    }
    public static void startPicker(Context context, CroperResultListener listener) {
        slistener = listener;
        Intent intent = new Intent(context, CropWrapperActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(START_PICKER, true);
        context.startActivity(intent);
    }

    public static void startColorPicker(Context context, CroperResultListener listener) {
        slistener = listener;
        Intent intent = new Intent(context, CropWrapperActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(START_COLOR_PICKER, true);
        context.startActivity(intent);
    }
}
