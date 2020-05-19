package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.ContextMenu;
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
import com.liadpaz.amp.databinding.FragmentSongsListBinding;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.QueueUtil;

public class SongsListFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = "SONGS_LIST_FRAGMENT";

    private SongsListAdapter adapter;

    private FragmentSongsListBinding binding;

    public SongsListFragment() { }

    @SuppressWarnings("unused")
    @NonNull
    public static SongsListFragment newInstance() {
        return new SongsListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentSongsListBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new SongsListAdapter(getContext(), (v, position) -> {
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
        adapter.submitList(LocalFiles.listSongs(getContext()));

        binding.rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSongs.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        binding.rvSongs.setAdapter(adapter);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater()
                     .inflate(R.menu.menu_song, menu);
    }
}
