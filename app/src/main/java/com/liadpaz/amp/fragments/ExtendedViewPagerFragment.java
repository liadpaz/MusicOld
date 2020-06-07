package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.adapters.ExtendedViewPagerAdapter;
import com.liadpaz.amp.databinding.FragmentExtendedViewPagerBinding;
import com.liadpaz.amp.livedatautils.QueueUtil;

public class ExtendedViewPagerFragment extends Fragment {
    private static final String TAG = "AmpApp.ExtendedViewPagerFragment";

    private boolean isCreated = false;

    private FragmentExtendedViewPagerBinding binding;

    public ExtendedViewPagerFragment() {}

    @NonNull
    public static ExtendedViewPagerFragment newInstance() { return new ExtendedViewPagerFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentExtendedViewPagerBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.extendedViewPager.setAdapter(new ExtendedViewPagerAdapter(this));
        QueueUtil.observePosition(getViewLifecycleOwner(), queuePosition -> {
            if (!isCreated) {
                isCreated = true;
                QueueUtil.isChanging = true;
            }
            binding.extendedViewPager.setCurrentItem(queuePosition);
        });
    }
}