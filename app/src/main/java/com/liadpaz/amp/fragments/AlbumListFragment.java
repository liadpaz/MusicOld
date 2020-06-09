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

import com.liadpaz.amp.livedatautils.SongsUtil;
import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.AlbumsListAdapter;
import com.liadpaz.amp.databinding.FragmentAlbumListBinding;
import com.liadpaz.amp.viewmodels.Album;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumListFragment extends Fragment {
    private ArrayList<Album> albums = new ArrayList<>();

    private FragmentAlbumListBinding binding;

    public AlbumListFragment() {}

    @NonNull
    public static AlbumListFragment newInstance() { return new AlbumListFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentAlbumListBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AlbumsListAdapter adapter = new AlbumsListAdapter(getContext(), (v, position) -> {
            Fragment fragment = AlbumSongListFragment.newInstance(albums.get(position));
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainFragment, fragment).addToBackStack(null).commit();
        });

        binding.rvAlbums.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAlbums.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL) );
        binding.rvAlbums.setAdapter(adapter);

        SongsUtil.observe(this, songs -> {
            albums.clear();
            HashMap<String, ArrayList<Song>> albumsMap = new HashMap<>();
            for (final Song song : songs) {
                if (albumsMap.containsKey(song.album)) {
                    albumsMap.get(song.album).add(song);
                } else {
                    albumsMap.put(song.album, new ArrayList<Song>() {{
                        add(song);
                    }});
                }
            }
            albumsMap.forEach((name, albumSongs) -> albums.add(new Album(name, albumSongs.get(0).songArtists.get(0), albumSongs)));
            albums.sort((album1, album2) -> album1.name.toLowerCase().compareTo(album2.name.toLowerCase()));
            adapter.submitList(albums);
        });
    }
}
