package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentArtistsBinding;

public class ArtistsFragment extends Fragment {

    public ArtistsFragment() { }

    @NonNull
    static ArtistsFragment newInstance() { return new ArtistsFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return FragmentArtistsBinding.inflate(inflater, container, false)
                                     .getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getChildFragmentManager().beginTransaction()
                                 .replace(R.id.containerArtists, ArtistListFragment.newInstance())
                                 .commit();
    }
}
