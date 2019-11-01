package com.spisoft.quicknote.intro;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class DisableOptimisationFragment extends Fragment {
    public DisableOptimisationFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sayhi_introduction, container, false);
        v.findViewById(R.id.sayhi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((HelpActivity)getActivity()).next();
            }
        });

        return v;
    }
}
