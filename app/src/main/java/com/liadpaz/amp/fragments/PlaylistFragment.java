package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.FragmentPlaylistBinding;
import com.liadpaz.amp.dialogs.PlaylistsDialog;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.PlaylistsUtil;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.viewmodels.Playlist;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.Collections;

public class PlaylistFragment extends Fragment {
    private static final String TAG = "PlaylistFragment";

    private SongsListAdapter adapter;

    private Playlist playlist;

    private FragmentPlaylistBinding binding;

    private PlaylistFragment(Playlist playlist) { this.playlist = playlist; }

    @NonNull
    public static PlaylistFragment newInstance(Playlist playlist) { return new PlaylistFragment(playlist); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentPlaylistBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new SongsListAdapter(requireContext(), (v, position) -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.inflate(playlist.name.equals(getString(R.string.playlist_recently_added)) ? R.menu.menu_playlist_recently : R.menu.menu_playlist);

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menuPlayNext: {
                        QueueUtil.addToNext(adapter.getCurrentList().get(position));
                        break;
                    }

                    case R.id.menuAddQueue: {
                        QueueUtil.addToEnd(adapter.getCurrentList().get(position));
                        break;
                    }

                    case R.id.menuQueueAddPlaylist: {
                        new PlaylistsDialog(adapter.getCurrentList().get(position)).show(getChildFragmentManager(), null);
                        break;
                    }

                    case R.id.menuRemoveFromPlaylist: {
                        Playlist playlist = PlaylistsUtil.removePlaylist(PlaylistFragment.this.playlist.name);
                        playlist.songs.remove(position);
                        adapter.submitList(playlist.songs);
                        adapter.notifyDataSetChanged();
                        PlaylistsUtil.addPlaylist(playlist);
                        break;
                    }
                }
                return true;
            });

            popupMenu.show();
        }, v -> {
            ArrayList<Song> queue = new ArrayList<>(playlist.songs);
            Collections.shuffle(queue);
            QueueUtil.queue.setValue(queue);
            QueueUtil.setPosition(0);
            MainActivity.getController().sendCommand(Constants.ACTION_QUEUE_POSITION, null, null);
        });

        binding.tvPlaylistName.setText(playlist.name);
        binding.rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSongs.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.rvSongs.setAdapter(adapter);

        if (playlist.name.equals(getString(R.string.playlist_recently_added))) {
            binding.btnDelete.setVisibility(View.GONE);
        } else {
            binding.btnDelete.setOnClickListener(v -> {
                PlaylistsUtil.removePlaylist(playlist.name);
                requireActivity().onBackPressed();
            });

            PlaylistsUtil.observe(requireActivity(), playlists -> {
                for (Playlist playlist : playlists) {
                    if (playlist.name.equals(this.playlist.name)) {
                        Log.d(TAG, "onViewCreated: " + playlist.songs.size());
                        adapter.submitList(playlist.songs);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
            });
        }

        adapter.submitList(playlist.songs);
    }
}
