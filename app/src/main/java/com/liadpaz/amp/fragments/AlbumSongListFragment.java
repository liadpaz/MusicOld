package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.FragmentAlbumSongListBinding;
import com.liadpaz.amp.dialogs.PlaylistsDialog;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.viewmodels.Album;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.Collections;

public class AlbumSongListFragment extends Fragment {
    private SongsListAdapter adapter;

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

        adapter = new SongsListAdapter(requireContext(), (v, position) -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.inflate(R.menu.menu_song);
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

                    case R.id.menuAddToPlaylist: {
                        new PlaylistsDialog(adapter.getCurrentList().get(position)).show(getChildFragmentManager(), null);
                        break;
                    }
                }
                return true;
            });
            popupMenu.show();
        }, v -> {
            ArrayList<Song> queue = new ArrayList<>(album.songs);
            Collections.shuffle(queue);
            QueueUtil.queue.setValue(queue);
            QueueUtil.setPosition(0);
            MainActivity.getController().sendCommand(Constants.ACTION_QUEUE_POSITION, null, null);
        });
        adapter.submitList(album.songs);

        binding.rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSongs.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.rvSongs.setAdapter(adapter);
    }
}
