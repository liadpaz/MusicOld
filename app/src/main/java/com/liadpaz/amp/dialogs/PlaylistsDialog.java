package com.liadpaz.amp.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.DialogPlaylistsBinding;
import com.liadpaz.amp.livedatautils.PlaylistsUtil;
import com.liadpaz.amp.viewmodels.Playlist;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;

public class PlaylistsDialog extends DialogFragment {
    private static final String TAG = "AmpApp.PlaylistsDialog";

    private Song song;
    private ArrayList<Song> songs;

    public PlaylistsDialog(@NonNull Song song) {
        this.song = song;
    }

    public PlaylistsDialog(@NonNull ArrayList<Song> songs) {
        this.songs = songs;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogPlaylistsBinding binding = DialogPlaylistsBinding.inflate(inflater, container, false);

        if (song != null) {
            binding.tvAddToPlaylist.setText(song.songTitle);
        }
        binding.spinnerPlaylists.setAdapter(new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, PlaylistsUtil.getPlaylistsNames()));
        binding.btnNewPlaylist.setOnClickListener(v -> {
            if (songs == null) {
                new NewPlaylistDialog(song).show(requireParentFragment().getChildFragmentManager(), null);
            } else {
                new NewPlaylistDialog(songs).show(requireParentFragment().getChildFragmentManager(), null);
            }
            dismiss();
        });
        binding.btnAdd.setOnClickListener(v -> {
            if (binding.spinnerPlaylists.getSelectedItem() != null) {
                Playlist playlist = PlaylistsUtil.removePlaylist((String)binding.spinnerPlaylists.getSelectedItem());
                if (song != null) {
                    playlist.songs.add(song);
                } else {
                    playlist.songs.addAll(songs);
                }
                PlaylistsUtil.addPlaylist(playlist);
                dismiss();
            }
        });
        binding.btnCancel.setOnClickListener(v -> dismiss());

        setCancelable(true);

        return binding.getRoot();
    }
}
