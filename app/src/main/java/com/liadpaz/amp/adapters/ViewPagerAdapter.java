package com.liadpaz.amp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private ArrayList<Class> fragments;

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, @NonNull ArrayList<Class> fragments) {
        super(fragmentManager, lifecycle);
        this.fragments = fragments;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        try {
            return (Fragment)fragments.get(position).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
