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

import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.PlaylistsAdapter;
import com.liadpaz.amp.databinding.FragmentPlaylistsBinding;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.viewmodels.Playlist;

import java.util.ArrayList;

public class PlaylistsFragment extends Fragment {

    private FragmentPlaylistsBinding binding;

    public PlaylistsFragment() { }

    @NonNull
    public static PlaylistsFragment newInstance() { return new PlaylistsFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentPlaylistsBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ArrayList<Playlist> playlists = new ArrayList<Playlist>() {
            { add(new Playlist(getString(R.string.playlist_recently_added), LocalFiles.listSongsByLastAdded(requireContext()))); }
        };

        PlaylistsAdapter adapter = new PlaylistsAdapter(requireContext(), (v, position) -> requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.viewpagerFragment, PlaylistFragment.newInstance(playlists.get(position))).addToBackStack(null).commit());

        binding.rvPlaylists.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPlaylists.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.rvPlaylists.setAdapter(adapter);

        adapter.submitList(playlists);
    }
}
