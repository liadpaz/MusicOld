package com.liadpaz.amp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.liadpaz.amp.livedatautils.QueueUtil;

public class ExtendedViewPagerAdapter extends FragmentStateAdapter {

    public ExtendedViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return null; //ExtendedSongFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return QueueUtil.getQueueSize();
    }
}
