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

import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.FragmentArtistSongListBinding;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.viewmodels.Artist;

public class ArtistSongListFragment extends Fragment {
    private SongsListAdapter adapter;

    private FragmentArtistSongListBinding binding;

    public ArtistSongListFragment() { }

    public static ArtistSongListFragment newInstance() { return new ArtistSongListFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentArtistSongListBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Artist artist = getArguments().getParcelable(Constants.ARTIST);

        binding.setArtist(artist);

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

                    case R.id.menuQueueAddPlaylist: {
                        // TODO: add to playlist
                        break;
                    }
                }
                return true;
            });
            popupMenu.show();
        });
        adapter.submitList(artist.songs);

        binding.rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSongs.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.rvSongs.setAdapter(adapter);
    }
}
