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

import com.liadpaz.amp.livedatautils.PlaylistsUtil;
import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.PlaylistsAdapter;
import com.liadpaz.amp.databinding.FragmentPlaylistsBinding;
import com.liadpaz.amp.dialogs.EditPlaylistDialog;
import com.liadpaz.amp.dialogs.NewPlaylistDialog;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.viewmodels.Playlist;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragment extends Fragment {
    private Playlist recentlyAddedPlaylist;
    private List<Playlist> playlists;

    private PlaylistsAdapter adapter;

    private FragmentPlaylistsBinding binding;

    public PlaylistsFragment() { }

    @NonNull
    public static PlaylistsFragment newInstance() { return new PlaylistsFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentPlaylistsBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recentlyAddedPlaylist = new Playlist(getString(R.string.playlist_recently_added), LocalFiles.listSongsByLastAdded(requireContext()));
        playlists = new ArrayList<Playlist>() {{
            add(recentlyAddedPlaylist);
        }};

        PlaylistsUtil.observe(requireActivity(), playlists -> {
            this.playlists = new ArrayList<>(playlists);
            this.playlists.add(0, recentlyAddedPlaylist);
            LocalFiles.setPlaylists(playlists);
            if (adapter != null) {
                adapter.submitList(this.playlists);
            }
        });

        adapter = new PlaylistsAdapter(requireContext(), (v, position) -> requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainFragment, PlaylistFragment.newInstance(playlists.get(position))).addToBackStack(null).commit(), position -> {
            if (position != 0) {
                new EditPlaylistDialog(playlists.get(position)).show(getChildFragmentManager(), null);
            }
            return true;
        });

        binding.rvPlaylists.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPlaylists.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.rvPlaylists.setAdapter(adapter);

        adapter.submitList(playlists);

        binding.fabNewPlaylist.setOnClickListener(v -> new NewPlaylistDialog((Song)null).show(getChildFragmentManager(), null));
    }
}
