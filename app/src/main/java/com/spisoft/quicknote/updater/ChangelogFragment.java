package com.spisoft.quicknote.updater;


import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spisoft.quicknote.R;
import com.spisoft.quicknote.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChangelogFragment extends Fragment {


    private TextView mChangelogView;

    public ChangelogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_changelog, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mChangelogView = view.findViewById(R.id.changelog);
        new AsyncTask<Void, Void, String>(){

            @Override
            protected String doInBackground(Void... voids) {
                AssetManager assetManager = getContext().getAssets();

                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open("CHANGELOG.md");
                    return FileUtils.readInputStream(in);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
            protected void onPostExecute(String result) {
                mChangelogView.setText(result);
            }
        }.execute();
    }


    public void setOnTouchListener(final View.OnTouchListener onTouchListener) {
        mChangelogView.setOnTouchListener(onTouchListener);
    }
}
