package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.liadpaz.amp.livedatautils.PlaylistsUtil;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.PlaylistAdapter;
import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.FragmentPlaylistBinding;
import com.liadpaz.amp.dialogs.PlaylistsDialog;
import com.liadpaz.amp.interfaces.ItemTouchHelperAdapter;
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.viewmodels.Playlist;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.Collections;

public class PlaylistFragment extends Fragment {
    private static final String TAG = "AmpApp.PlaylistFragment";

    private boolean isChanging;

    private ListAdapter<Song, ? extends RecyclerView.ViewHolder> adapter;

    private Playlist playlist;

    private FragmentPlaylistBinding binding;

    private PlaylistFragment(Playlist playlist) { this.playlist = playlist; }

    @NonNull
    public static PlaylistFragment newInstance(Playlist playlist) { return new PlaylistFragment(playlist); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentPlaylistBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        OnRecyclerItemClickListener onMoreClicked = (v, position) -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.inflate(playlist.name.equals(getString(R.string.playlist_recently_added)) ? R.menu.menu_playlist_recently : R.menu.menu_playlist);

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
                        new PlaylistsDialog(adapter.getCurrentList().get(position)).show(getChildFragmentManager(), null);
                        break;
                    }

                    case R.id.menuRemoveFromPlaylist: {
                        Playlist playlist = PlaylistsUtil.removePlaylist(PlaylistFragment.this.playlist.name);
                        playlist.songs.remove(position);
                        adapter.submitList(playlist.songs);
                        adapter.notifyDataSetChanged();
                        PlaylistsUtil.addPlaylist(playlist);
                        break;
                    }
                }
                return true;
            });

            popupMenu.show();
        };
        View.OnClickListener onShuffleClickListener = v -> {
            ArrayList<Song> queue = new ArrayList<>(playlist.songs);
            Collections.shuffle(queue);
            QueueUtil.queue.setValue(queue);
            QueueUtil.setPosition(0);
            MainActivity.getController().sendCommand(Constants.ACTION_QUEUE_POSITION, null, null);
        };

        if (playlist.name.equals(getString(R.string.playlist_recently_added))) {
            adapter = new SongsListAdapter(requireContext(), onMoreClicked, onShuffleClickListener);
            binding.btnDelete.setVisibility(View.GONE);
        } else {
            adapter = new PlaylistAdapter(requireContext(), onMoreClicked, onShuffleClickListener, new ItemTouchHelperAdapter() {
                @Override
                public void onItemMove(int fromPosition, int toPosition) {
                    isChanging = true;
                    Playlist playlist = PlaylistsUtil.removePlaylist(PlaylistFragment.this.playlist.name);
                    Collections.swap(playlist.songs, fromPosition, toPosition);
                    PlaylistsUtil.addPlaylist(playlist);
                }

                @Override
                public void onItemDismiss(int position) {}
            });
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                @Override
                public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                    return makeMovementFlags(viewHolder.getAdapterPosition() == 0 ? 0 : ItemTouchHelper.DOWN | ItemTouchHelper.UP, 0);
                }

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    Log.d(TAG, "onItemMove: from: " + viewHolder.getAdapterPosition() + " to: " + target.getAdapterPosition());
                    int toPosition = target.getAdapterPosition() == 0 ? 1 : target.getAdapterPosition();
                    ((ItemTouchHelperAdapter)adapter).onItemMove(viewHolder.getAdapterPosition(), toPosition);
                    return true;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
            });
            ((PlaylistAdapter)adapter).setOnStartDragListener(itemTouchHelper::startDrag);
            itemTouchHelper.attachToRecyclerView(binding.rvSongs);
            binding.btnDelete.setOnClickListener(v -> {
                PlaylistsUtil.removePlaylist(playlist.name);
                getParentFragmentManager().popBackStack();
            });

            PlaylistsUtil.observe(requireActivity(), playlists -> {
                if (!isChanging) {
                    for (Playlist playlist : playlists) {
                        if (playlist.name.equals(this.playlist.name)) {
                            adapter.submitList(playlist.songs);
                            adapter.notifyDataSetChanged();
                            return;
                        }
                    }
                } else {
                    isChanging = false;
                }
            });
        }

        binding.tvPlaylistName.setText(playlist.name);
        binding.rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSongs.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.rvSongs.setAdapter(adapter);

        adapter.submitList(playlist.songs);
    }
}
