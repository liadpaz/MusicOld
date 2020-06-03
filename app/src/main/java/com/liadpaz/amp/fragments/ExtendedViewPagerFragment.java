package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.databinding.FragmentExtendedViewPagerBinding;

public class ExtendedViewPagerFragment extends Fragment {
    private static final String TAG = "AmpApp.ExtendedViewPagerFragment";

    private FragmentExtendedViewPagerBinding binding;

    public ExtendedViewPagerFragment() {}

    public static ExtendedViewPagerFragment newInstance() { return new ExtendedViewPagerFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentExtendedViewPagerBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
    }
}