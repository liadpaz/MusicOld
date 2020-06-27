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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.DialogEditPlaylistBinding;
import com.liadpaz.amp.livedatautils.PlaylistsUtil;
import com.liadpaz.amp.viewmodels.Playlist;

public class EditPlaylistDialog extends DialogFragment {
    private Playlist playlist;

    public EditPlaylistDialog(@NonNull Playlist playlist) {
        this.playlist = playlist;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogEditPlaylistBinding binding = DialogEditPlaylistBinding.inflate(inflater, container, false);

        binding.etPlaylistName.setText(playlist.name);
        binding.btnApply.setOnClickListener(v -> {
            String newName = binding.etPlaylistName.getText().toString();
            if (!TextUtils.isEmpty(newName)) {
                if (!PlaylistsUtil.isPlaylistExists(newName)) {
                    PlaylistsUtil.removePlaylist(playlist.name);
                    PlaylistsUtil.addPlaylist(new Playlist(newName, playlist.songs));
                    dismiss();
                } else {
                    Toast.makeText(getContext(), R.string.toast_playlist_exists, Toast.LENGTH_LONG).show();
                }
            }
        });
        binding.btnDelete.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.dialog_delete_playlist_title).setMessage(R.string.dialog_delete_playlist_content).setPositiveButton(R.string.dialog_yes, ((dialog, which) -> {
            PlaylistsUtil.removePlaylist(playlist.name);
            dismiss();
        })).setNegativeButton(R.string.dialog_no, null).show());
        binding.btnCancel.setOnClickListener(v -> dismiss());

        setCancelable(true);

        return binding.getRoot();
    }
}
