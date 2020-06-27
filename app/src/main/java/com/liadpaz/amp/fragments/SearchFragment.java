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
import androidx.recyclerview.widget.RecyclerView;

import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.SearchSongListAdapter;
import com.liadpaz.amp.databinding.FragmentSearchBinding;
import com.liadpaz.amp.dialogs.PlaylistsDialog;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.viewmodels.Song;

import java.util.List;

public class SearchFragment extends Fragment {
    private static final String TAG = "AmpApp.SearchFragment";

    private SearchSongListAdapter adapter;

    private String query;
    private List<Song> songs;

    private FragmentSearchBinding binding;

    private SearchFragment(@NonNull String query, @NonNull List<Song> songs) {
        this.query = query;
        this.songs = songs;
    }

    @NonNull
    public static SearchFragment newInstance(@NonNull String query, @NonNull List<Song> songs) { return new SearchFragment(query, songs); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentSearchBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.tvQuery.setText(getString(R.string.search_result, query));
        adapter = new SearchSongListAdapter(requireContext(), (v, position) -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.inflate(R.menu.menu_song);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menuPlayNext: {
                        QueueUtil.addToNext(adapter.getCurrentList().get(position));
                        break;
                    }

                    case R.id.menuAddQueue: {
                        QueueUtil.add(adapter.getCurrentList().get(position));
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
        });

        binding.rvSearchSongs.setAdapter(adapter);

        binding.rvSearchSongs.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        binding.rvSearchSongs.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        adapter.submitList(songs);
    }
}