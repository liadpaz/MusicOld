package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentNoSongBinding;
import com.liadpaz.amp.livedatautils.QueueUtil;

public class NoSongFragment extends Fragment {
    private static final String TAG = "AmpApp.NoSongFragment";

    private NoSongFragment() {}

    @NonNull
    public static NoSongFragment newInstance() { return new NoSongFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return FragmentNoSongBinding.inflate(inflater, container, false).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        QueueUtil.observeQueue(getViewLifecycleOwner(), songs -> {
            if (songs.size() != 0) {
                getParentFragmentManager().beginTransaction().replace(R.id.layoutFragment, ExtendedViewPagerFragment.newInstance()).commit();
            }
        });
    }
}