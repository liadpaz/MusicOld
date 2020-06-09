package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.ExtendedViewPagerAdapter;
import com.liadpaz.amp.databinding.FragmentExtendedViewPagerBinding;
import com.liadpaz.amp.livedatautils.QueueUtil;

public class ExtendedViewPagerFragment extends Fragment {
    private static final String TAG = "AmpApp.ExtendedViewPagerFragment";

    private ExtendedViewPagerAdapter adapter;
    private ViewPager2.OnPageChangeCallback callback;

    private boolean isCreated = false;

    private FragmentExtendedViewPagerBinding binding;

    private ExtendedViewPagerFragment() {}

    @NonNull
    public static ExtendedViewPagerFragment newInstance() { return new ExtendedViewPagerFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentExtendedViewPagerBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new ExtendedViewPagerAdapter(this);
        binding.extendedViewPager.setAdapter(adapter);
        QueueUtil.observePosition(getViewLifecycleOwner(), position -> {
            if (!isCreated) {
                isCreated = true;
                binding.extendedViewPager.setCurrentItem(position, false);
            } else {
                binding.extendedViewPager.setCurrentItem(position);
            }
        });
        QueueUtil.observeQueue(getViewLifecycleOwner(), songs -> {
            if (songs.size() == 0) {
                getParentFragmentManager().beginTransaction().replace(R.id.layoutFragment, NoSongFragment.newInstance()).commit();
            } else {
                binding.extendedViewPager.setAdapter(adapter = new ExtendedViewPagerAdapter(this));
            }
        });
        binding.extendedViewPager.registerOnPageChangeCallback(callback = new ViewPager2.OnPageChangeCallback() {
            private boolean firstTime = true;

            @Override
            public void onPageSelected(int position) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    QueueUtil.setPosition(position);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (callback != null) {
            binding.extendedViewPager.unregisterOnPageChangeCallback(callback);
        }
    }
}