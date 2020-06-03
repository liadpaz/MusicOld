package com.liadpaz.amp.dialogs;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.DialogNewPlaylistBinding;
import com.liadpaz.amp.LiveDataUtils.PlaylistsUtil;
import com.liadpaz.amp.viewmodels.Playlist;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;

public class NewPlaylistDialog extends DialogFragment {
    private static final String TAG = "AmpApp.NewPlaylistDialog";

    private Song song;
    private ArrayList<Song> songs;

    public NewPlaylistDialog(@Nullable Song song) {
        this.song = song;
    }

    public NewPlaylistDialog(@NonNull ArrayList<Song> songs) {
        this.songs = songs;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogNewPlaylistBinding binding = DialogNewPlaylistBinding.inflate(inflater, container, false);

        binding.btnCreate.setOnClickListener(v -> {
            String name = binding.etPlaylistName.getText().toString();
            if (!TextUtils.isEmpty(name)) {
                if (!PlaylistsUtil.isPlaylistExists(name)) {
                    PlaylistsUtil.addPlaylist(new Playlist(name, new ArrayList<Song>() {{
                        if (song != null) {
                            add(song);
                        } else if (songs != null) {
                            addAll(songs);
                        }
                    }}));
                    dismiss();
                } else {
                    Toast.makeText(getContext(), R.string.toast_playlist_exists, Toast.LENGTH_LONG).show();
                }
            }
        });
        binding.btnCancel.setOnClickListener(v -> dismiss());

        setCancelable(true);

        return binding.getRoot();
    }
}
