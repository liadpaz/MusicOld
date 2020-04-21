package com.liadpaz.music.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.liadpaz.music.adapters.ViewPagerAdapter;
import com.liadpaz.music.databinding.FragmentViewPagerBinding;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass. Use the {@link ViewPagerFragment#newInstance} factory method
 * to create an instance of this fragment.
 */
public class ViewPagerFragment extends Fragment {

    private static final String TAG = "VIEWPAGER_FRAGMENT";

    private ViewPager2 viewPager;

    private FragmentViewPagerBinding binding;

    public ViewPagerFragment() {
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @return A new instance of fragment ViewPagerFragment.
     */
    private static ViewPagerFragment newInstance() {
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: CREATING");
        return (binding = FragmentViewPagerBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager = binding.viewPager;

        viewPager.setAdapter(new ViewPagerAdapter(this, new ArrayList<Class>() {{
            add(SongsListFragment.class);
            add(BlankFragment.class);
        }}));

        ArrayList<String> tabsTitle = new ArrayList<String>() {{
            add("Songs");
            add("Blank");
        }};

        new TabLayoutMediator(binding.tabLayout, viewPager, ((tab, position) -> tab.setText(tabsTitle.get(position)))).attach();
    }
}
