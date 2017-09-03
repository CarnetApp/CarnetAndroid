package com.spisoft.quicknote.noise;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spisoft.quicknote.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class NoiseSelectorFragment extends Fragment {

    public NoiseSelectorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_noise_selector, container, false);
    }
}
