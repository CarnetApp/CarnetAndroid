package com.spisoft.quicknote.intro;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spisoft.quicknote.R;


public class WelcomeIntroductionFragment extends Fragment {
    public WelcomeIntroductionFragment() {
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
        return inflater.inflate(R.layout.fragment_welcome_introduction, container, false);
    }

    @Override
    public void onViewCreated(final View v, Bundle saved){
        v.findViewById(R.id.versatile_tv).animate().alpha(1).setDuration(500).setStartDelay(500).start();
        v.findViewById(R.id.open_tv).animate().alpha(1).setDuration(500).setStartDelay(1000).start();
        v.findViewById(R.id.doesnt_spy_tv).animate().alpha(1).setDuration(500).setStartDelay(1500).start();
    }
}
