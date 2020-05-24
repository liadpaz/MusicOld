package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.ViewPagerAdapter;
import com.liadpaz.amp.databinding.FragmentMainBinding;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    public MainFragment() { }

    @NonNull
    public static MainFragment newInstance() { return new MainFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentMainBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity)requireActivity()).setSupportActionBar(binding.toolBarMain);

        ViewPager2 viewPager = binding.viewPagerMain;

        viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), getLifecycle(), new ArrayList<Class>() {{
            add(SongsListFragment.class);
            add(PlaylistsFragment.class);
            add(ArtistsFragment.class);
            add(AlbumsFragment.class);
        }}));

        ArrayList<String> tabsTitle = new ArrayList<String>() {{
            add(getString(R.string.tab_songs));
            add(getString(R.string.tab_playlists));
            add(getString(R.string.tab_artists));
            add(getString(R.string.tab_albums));
        }};

        new TabLayoutMediator(binding.tabLayoutMain, viewPager, ((tab, position) -> tab.setText(tabsTitle.get(position)))).attach();

//        getChildFragmentManager().beginTransaction().replace(R.id.controllerFragment, ControllerFragment.newInstance()).commit();
    }
}
