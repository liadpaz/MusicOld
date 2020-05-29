package com.liadpaz.amp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.DialogPlaylistsBinding;
import com.liadpaz.amp.utils.PlaylistsUtil;
import com.liadpaz.amp.viewmodels.Song;

public class PlaylistsDialog extends Dialog {
    private Song song;

    public PlaylistsDialog(@NonNull Context context, @NonNull Song song) {
        super(context);
        this.song = song;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogPlaylistsBinding binding = DialogPlaylistsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvAddToPlaylist.setText(song.songTitle);
        binding.spinnerPlaylists.setAdapter(new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, PlaylistsUtil.getPlaylistsNames()));
        binding.btnNewPlaylist.setOnClickListener(v -> {
            new NewPlaylistDialog(getContext(), song).show();
            dismiss();
        });
        binding.btnAdd.setOnClickListener(v -> {
            PlaylistsUtil.getPlaylistByName((String)binding.spinnerPlaylists.getSelectedItem()).songs.add(song);
            dismiss();
        });
        binding.btnCancel.setOnClickListener(v -> dismiss());

        setCancelable(true);
    }
}
