package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.MainViewPagerAdapter;
import com.liadpaz.amp.databinding.FragmentViewPagerBinding;

import java.util.ArrayList;

public class ViewPagerFragment extends Fragment {
    private static final String TAG = "AmpApp.ViewPagerFragment";

    private FragmentViewPagerBinding binding;

    public ViewPagerFragment() { }

    @NonNull
    public static ViewPagerFragment newInstance() { return new ViewPagerFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentViewPagerBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewPager2 viewPager = binding.viewPagerMain;

        viewPager.setAdapter(new MainViewPagerAdapter(getChildFragmentManager(), getLifecycle()));

        ArrayList<String> tabsTitle = new ArrayList<String>() {{
            add(getString(R.string.tab_songs));
            add(getString(R.string.tab_playlists));
            add(getString(R.string.tab_artists));
            add(getString(R.string.tab_albums));
        }};

        new TabLayoutMediator(binding.tabLayoutMain, viewPager, ((tab, position) -> tab.setText(tabsTitle.get(position)))).attach();
    }
}
