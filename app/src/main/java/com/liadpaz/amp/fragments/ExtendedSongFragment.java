package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentExtendedSongBinding;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.utils.Utilities;

public class ExtendedSongFragment extends Fragment {
    private static final String TAG = "AmpApp.ExtendedSongFragment";

    private int position;

    private FragmentExtendedSongBinding binding;

    private ExtendedSongFragment(int position) { this.position = position; }

    @NonNull
    public static ExtendedSongFragment newInstance(int position) { return new ExtendedSongFragment(position); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentExtendedSongBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Glide.with(this).load(Utilities.getCoverUri(QueueUtil.getQueue().get(position))).placeholder(R.drawable.song).into(binding.ivSongCover);
    }
}
