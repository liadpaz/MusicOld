package com.liadpaz.amp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.liadpaz.amp.fragments.AlbumListFragment;
import com.liadpaz.amp.fragments.ArtistListFragment;
import com.liadpaz.amp.fragments.PlaylistsFragment;
import com.liadpaz.amp.fragments.SongsListFragment;
import com.liadpaz.amp.livedatautils.SongsUtil;

public class MainViewPagerAdapter extends FragmentStateAdapter {

    public MainViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: {
                return SongsListFragment.newInstance(SongsUtil.getSongs());
            }

            case 1: {
                return PlaylistsFragment.newInstance();
            }

            case 2: {
                return ArtistListFragment.newInstance();
            }

            case 3: {
                return AlbumListFragment.newInstance();
            }
        }
        throw new IndexOutOfBoundsException("Max 4 pages!");
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
