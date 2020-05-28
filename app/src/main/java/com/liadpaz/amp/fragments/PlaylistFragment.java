package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.FragmentPlaylistBinding;
import com.liadpaz.amp.viewmodels.Playlist;

public class PlaylistFragment extends Fragment {
    private Playlist playlist;

    private FragmentPlaylistBinding binding;

    private PlaylistFragment(Playlist playlist) { this.playlist = playlist; }

    public static PlaylistFragment newInstance(Playlist playlist) { return new PlaylistFragment(playlist); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentPlaylistBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SongsListAdapter adapter = new SongsListAdapter(requireContext(), (v, position) -> {});

        binding.tvPlaylistName.setText(playlist.name);
        binding.rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSongs.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.rvSongs.setAdapter(adapter);

        adapter.submitList(playlist.songs);
    }
}
