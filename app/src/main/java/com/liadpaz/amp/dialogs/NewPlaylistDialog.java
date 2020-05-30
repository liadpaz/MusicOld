package com.liadpaz.amp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.DialogNewPlaylistBinding;
import com.liadpaz.amp.utils.PlaylistsUtil;
import com.liadpaz.amp.viewmodels.Playlist;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;

public class NewPlaylistDialog extends Dialog {
    private Song song;

    public NewPlaylistDialog(@NonNull Context context, @Nullable Song song) {
        super(context);
        this.song = song;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogNewPlaylistBinding binding = DialogNewPlaylistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnCreate.setOnClickListener(v -> {
            String name = binding.etPlaylistName.getText().toString();
            if (!TextUtils.isEmpty(name)) {
                if (!PlaylistsUtil.isPlaylistExists(name)) {
                    PlaylistsUtil.addPlaylist(new Playlist(name, new ArrayList<Song>() {{
                        if (song != null) {
                            add(song);
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
    }
}
