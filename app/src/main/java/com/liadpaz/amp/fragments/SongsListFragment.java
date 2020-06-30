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
import com.liadpaz.amp.adapters.SongsListAdapter;
import com.liadpaz.amp.databinding.FragmentSongsListBinding;
import com.liadpaz.amp.dialogs.PlaylistsDialog;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.service.ServiceConnector;
import com.liadpaz.amp.utils.AmpPlaybackPreparer;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongsListFragment extends Fragment {
    private static final String TAG = "AmpApp.SongsListFragment";

    private List<Song> songs;

    private SongsListAdapter adapter;

    private FragmentSongsListBinding binding;

    public SongsListFragment(@NonNull List<Song> songs) { this.songs = songs; }

    @SuppressWarnings("unused")
    @NonNull
    public static SongsListFragment newInstance(@NonNull List<Song> songs) { return new SongsListFragment(songs); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentSongsListBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new SongsListAdapter(requireContext(), (v, position) -> new PopupMenu(requireContext(), v) {
            {
                inflate(R.menu.menu_song);
                setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.menuPlayNext: {
                            AmpPlaybackPreparer.addToQueueNext(songs.get(position));
                            break;
                        }

                        case R.id.menuAddQueue: {
                            AmpPlaybackPreparer.addToQueue(songs.get(position));
                            break;
                        }

                        case R.id.menuAddToPlaylist: {
                            new PlaylistsDialog(adapter.getCurrentList().get(position)).show(getChildFragmentManager(), null);
                            break;
                        }
                    }
                    return true;
                });
            }
        }.show(), v -> {
            ArrayList<Song> queue = new ArrayList<>(adapter.getCurrentList());
            Collections.shuffle(queue);
            QueueUtil.setQueue(queue);
            QueueUtil.setPosition(0);
            ServiceConnector.playFromQueue();
        }) {{
            submitList(songs);
            binding.rvSongs.setAdapter(this);
        }};

        binding.rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()) {{
            setSmoothScrollbarEnabled(true);
        }});
        binding.rvSongs.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.rvSongs.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0 && ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition() == 0) {
                    binding.fabScrollUp.setVisibility(View.GONE);
                } else if (binding.fabScrollUp.getVisibility() != View.VISIBLE) {
                    binding.fabScrollUp.setVisibility(View.VISIBLE);
                }
            }
        });
        binding.fabScrollUp.setOnClickListener(v -> {
            binding.rvSongs.scrollToPosition(0);
            v.setVisibility(View.GONE);
        });
    }
}
