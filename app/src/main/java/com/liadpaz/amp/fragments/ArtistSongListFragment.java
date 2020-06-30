package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentArtistSongListBinding;
import com.liadpaz.amp.viewmodels.Artist;

public class ArtistSongListFragment extends Fragment {
    private static final String TAG = "AmpApp.ArtistSongListFragment";

    private Artist artist;

    private FragmentArtistSongListBinding binding;

    private ArtistSongListFragment(Artist artist) { this.artist = artist; }

    @NonNull
    public static ArtistSongListFragment newInstance(Artist artist) { return new ArtistSongListFragment(artist); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentArtistSongListBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.setArtist(artist);
        getChildFragmentManager().beginTransaction().replace(R.id.containerFragment, SongsListFragment.newInstance(artist.songs)).commit();
    }
}
