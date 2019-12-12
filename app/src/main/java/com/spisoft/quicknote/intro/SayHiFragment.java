package com.spisoft.quicknote.intro;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

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


public class SayHiFragment extends Fragment {
    public SayHiFragment() {
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
                new Thread(){
                    public void run(){
                        HttpsURLConnection urlConnection = null;
                        try {
                            int version = -1;
                            try {
                                PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
                                version = pInfo.versionCode;
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            URL url = new URL("https://qn.phie.ovh/hi.php?type=android&version="+ version);
                            urlConnection = (HttpsURLConnection) url.openConnection();

                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(urlConnection.getInputStream()));
                            String inputLine;
                            StringBuffer response = new StringBuffer();

                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if(urlConnection!=null)
                                urlConnection.disconnect();
                        }
                    }
                }.start();
                PreferenceHelper.getInstance(getContext()).setSayHi(true);
                ((HelpActivity)getActivity()).next();
            }
        });

        return v;
    }
}
