package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.livedatautils.SongsUtil;
import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.ArtistsListAdapter;
import com.liadpaz.amp.databinding.FragmentArtistListBinding;
import com.liadpaz.amp.viewmodels.Artist;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.HashMap;

public class ArtistListFragment extends Fragment {
    private static final String TAG = "AmpApp.ArtistListFragment";

    private ArrayList<Artist> artists = new ArrayList<>();

    private ArtistsListAdapter adapter;

    private FragmentArtistListBinding binding;

    public ArtistListFragment() {}

    @NonNull
    public static ArtistListFragment newInstance() { return new ArtistListFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentArtistListBinding.inflate(getLayoutInflater(), container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.lvArtists.setAdapter(adapter = new ArtistsListAdapter(requireContext(), new ArrayList<>(artists)));
        binding.lvArtists.setOnItemClickListener((parent, view1, position, id) -> requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, ArtistSongListFragment.newInstance(artists.get(position))).addToBackStack(null).commit());

        SongsUtil.observe(this, songs -> {
            artists.clear();
            HashMap<String, ArrayList<Song>> artistsMap = new HashMap<>();
            for (final Song song : songs) {
                for (String artist : song.songArtists) {
                    if (artistsMap.containsKey(artist)) {
                        artistsMap.get(artist).add(song);
                    } else {
                        artistsMap.put(artist, new ArrayList<Song>() {{
                            add(song);
                        }});
                    }
                }
            }
            artistsMap.forEach((name, artistSongs) -> artists.add(new Artist(name, artistSongs)));
            artists.sort((artist1, artist2) -> artist1.name.toLowerCase().compareTo(artist2.name.toLowerCase()));
            adapter.clear();
            adapter.addAll(artists);
        });
    }
}
