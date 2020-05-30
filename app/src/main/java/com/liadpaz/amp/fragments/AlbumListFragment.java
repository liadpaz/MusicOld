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
import com.liadpaz.amp.adapters.AlbumsListAdapter;
import com.liadpaz.amp.databinding.FragmentAlbumListBinding;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.viewmodels.Album;

import java.util.ArrayList;

public class AlbumListFragment extends Fragment {
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
        ArrayList<Album> albums = new ArrayList<>();
        LocalFiles.getAlbums().forEach((albumName, albumSongs) -> albums.add(new Album(albumName, albumSongs.get(0).songArtists.get(0), albumSongs)));
        albums.sort((album1, album2) -> album1.name.toLowerCase().compareTo(album2.name.toLowerCase()));

        AlbumsListAdapter adapter = new AlbumsListAdapter(getContext(), (v, position) -> {
            Fragment fragment = AlbumSongListFragment.newInstance(albums.get(position));
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.viewpagerFragment, fragment).addToBackStack(null).commit();
        });

        binding.rvAlbums.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAlbums.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL) );
        binding.rvAlbums.setAdapter(adapter);

        adapter.submitList(albums);
    }
}