package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.ArtistsListAdapter;
import com.liadpaz.amp.databinding.FragmentArtistListBinding;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.viewmodels.Artist;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass. Use the {@link ArtistListFragment#newInstance} factory method
 * to create an instance of this fragment.
 */
public class ArtistListFragment extends Fragment {
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
        ArrayList<Artist> artists = new ArrayList<>();
        LocalFiles.getArtists().forEach((artistName, artistSongs) -> artists.add(new Artist(artistName, artistSongs)));
        artists.sort((artist1, artist2) -> artist1.name.toLowerCase().compareTo(artist2.name.toLowerCase()));
        ArtistsListAdapter adapter = new ArtistsListAdapter(getContext(), artists);

        binding.lvArtists.setAdapter(adapter);
        binding.lvArtists.setOnItemClickListener((parent, view1, position, id) -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.ARTIST, adapter.getItem(position));
            Fragment fragment = ArtistSongListFragment.newInstance();
            fragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.viewpagerFragment, fragment).addToBackStack(null).commit();
        });
    }
}
