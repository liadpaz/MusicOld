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

import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.FragmentSongsListBinding;
import com.liadpaz.amp.dialogs.PlaylistsDialog;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.Collections;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ArrayList<Song> songs = LocalFiles.listSongsByName(requireContext());
        adapter = new SongsListAdapter(requireContext(), (v, position) -> {
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

                    case R.id.menuAddToPlaylist: {
                        new PlaylistsDialog(adapter.getCurrentList().get(position)).show(getChildFragmentManager(), null);
                        break;
                    }
                }
                return true;
            });
            popupMenu.show();
        }, v -> {
            ArrayList<Song> queue = new ArrayList<>(songs);
            Collections.shuffle(queue);
            QueueUtil.queue.setValue(queue);
            QueueUtil.setPosition(0);
            MainActivity.getController().sendCommand(Constants.ACTION_QUEUE_POSITION, null, null);
        });
        adapter.submitList(songs);

        binding.rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSongs.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.rvSongs.setAdapter(adapter);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_song, menu);
    }
}
