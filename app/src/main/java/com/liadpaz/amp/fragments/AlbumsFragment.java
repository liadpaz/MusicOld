package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentAlbumsBinding;

public class AlbumsFragment extends Fragment {

    public AlbumsFragment() {}

    @NonNull
    public static AlbumsFragment newInstance() { return new AlbumsFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return FragmentAlbumsBinding.inflate(inflater, container, false)
                                    .getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getChildFragmentManager().beginTransaction()
                                 .replace(R.id.containerAlbums, AlbumListFragment.newInstance())
                                 .commit();
    }
}
