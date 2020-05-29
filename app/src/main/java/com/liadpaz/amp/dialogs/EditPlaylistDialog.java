package com.liadpaz.amp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.DialogEditPlaylistBinding;
import com.liadpaz.amp.utils.PlaylistsUtil;
import com.liadpaz.amp.viewmodels.Playlist;

public class EditPlaylistDialog extends Dialog {
    private Playlist playlist;

    public EditPlaylistDialog(@NonNull Context context, @NonNull Playlist playlist) {
        super(context);
        this.playlist = playlist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogEditPlaylistBinding binding = DialogEditPlaylistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.etPlaylistName.setText(playlist.name);
        binding.btnApply.setOnClickListener(v -> {
            if (!PlaylistsUtil.isPlaylistExists(binding.etPlaylistName.getText().toString())) {
                PlaylistsUtil.removePlaylist(playlist.name);
                PlaylistsUtil.addPlaylist(new Playlist(binding.etPlaylistName.getText().toString(), playlist.songs));
                dismiss();
            } else {
                Toast.makeText(getContext(), R.string.toast_playlist_exists, Toast.LENGTH_LONG).show();
            }
        });
        binding.btnDelete.setOnClickListener(v -> {
            PlaylistsUtil.removePlaylist(playlist.name);
            dismiss();
        });
        binding.btnCancel.setOnClickListener(v -> dismiss());

        setCancelable(true);
    }
}
