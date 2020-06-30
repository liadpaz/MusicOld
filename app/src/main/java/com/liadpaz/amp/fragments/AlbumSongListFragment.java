package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentAlbumSongListBinding;
import com.liadpaz.amp.viewmodels.Album;

public class AlbumSongListFragment extends Fragment {
    private static final String TAG = "AmpApp.AlbumSongListFragment";

    private Album album;

    private FragmentAlbumSongListBinding binding;

    private AlbumSongListFragment(Album album) { this.album = album; }

    @NonNull
    public static AlbumSongListFragment newInstance(Album album) { return new AlbumSongListFragment(album); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentAlbumSongListBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.setAlbum(album);
        getChildFragmentManager().beginTransaction().replace(R.id.containerFragment, SongsListFragment.newInstance(album.songs)).commit();
    }
}
