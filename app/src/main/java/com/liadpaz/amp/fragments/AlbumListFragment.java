package com.liadpaz.amp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.AlbumSongListActivity;
import com.liadpaz.amp.adapters.AlbumsListAdapter;
import com.liadpaz.amp.databinding.FragmentAlbumListBinding;
import com.liadpaz.amp.utils.Album;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;

import java.util.ArrayList;

public class AlbumListFragment extends Fragment {

    private static final String TAG = "ALBUM_LIST_FRAGMENT";
    private FragmentAlbumListBinding binding;

    public AlbumListFragment() {}

    @NonNull
    static AlbumListFragment newInstance() { return new AlbumListFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentAlbumListBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ArrayList<Album> albums = new ArrayList<>();
        LocalFiles.getAlbums().forEach((albumName, albumSongs) -> albums.add(new Album(albumName, albumSongs.get(0).getSongArtists().get(0), albumSongs)));
        albums.sort((album1, album2) -> album1.name.toLowerCase().compareTo(album2.name.toLowerCase()));

        AlbumsListAdapter adapter = new AlbumsListAdapter(getContext(), albums);
        binding.lvAlbums.setAdapter(adapter);

        binding.lvAlbums.setOnItemClickListener((parent, view1, position, id) -> startActivity(new Intent(getContext(), AlbumSongListActivity.class).putExtra(Constants.ALBUM, adapter.getItem(position).name)));
    }
}
