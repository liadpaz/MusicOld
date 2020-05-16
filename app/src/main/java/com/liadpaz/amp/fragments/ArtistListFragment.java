package com.liadpaz.amp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.ArtistSongListActivity;
import com.liadpaz.amp.adapters.ArtistsListAdapter;
import com.liadpaz.amp.databinding.FragmentArtistListBinding;
import com.liadpaz.amp.utils.Artist;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass. Use the {@link ArtistListFragment#newInstance} factory method
 * to create an instance of this fragment.
 */
public class ArtistListFragment extends Fragment {

    private static final String TAG = "ARTIST_LIST_FRAGMENT";

    private FragmentArtistListBinding binding;

    public ArtistListFragment() {}

    static ArtistListFragment newInstance() { return new ArtistListFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentArtistListBinding.inflate(getLayoutInflater(), container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ArrayList<Artist> artists = new ArrayList<>();
        LocalFiles.getArtists().forEach((artistName, artistSongs) -> artists.add(new Artist(artistName, artistSongs)));
        artists.sort((artist1, artist2) -> artist1.name.compareTo(artist2.name));
        ArtistsListAdapter adapter = new ArtistsListAdapter(getContext(), artists);

        binding.lvArtists.setAdapter(adapter);
        binding.lvArtists.setOnItemClickListener((parent, view1, position, id) -> startActivity(new Intent(getContext(), ArtistSongListActivity.class).putExtra(Constants.ARTIST, adapter.getItem(position).name)));
    }
}
