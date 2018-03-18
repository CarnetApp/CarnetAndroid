package com.spisoft.quicknote.synchro;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spisoft.quicknote.R;
import com.spisoft.sync.wrappers.googledrive.DriveSyncWrapper;


public class SynchroIntroductionFragment extends Fragment {


    public SynchroIntroductionFragment() {
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
        return inflater.inflate(R.layout.fragment_synchro_introduction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.gdrive_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DriveSyncWrapper(getActivity(), 0).authorize(getActivity());
            }
        });
        view.findViewById(R.id.nextcloud_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((HelpActivity)getActivity()).next();
            }
        });
    }

}
