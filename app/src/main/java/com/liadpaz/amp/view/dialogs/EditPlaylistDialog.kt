package com.liadpaz.amp.view.dialogs

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.DialogEditPlaylistBinding
import com.liadpaz.amp.viewmodels.livedatautils.PlaylistsUtil
import com.liadpaz.amp.view.data.Playlist

class EditPlaylistDialog(private val playlist: Playlist) : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DialogEditPlaylistBinding.inflate(inflater, container, false)
        binding.etPlaylistName.setText(playlist.name)
        binding.btnApply.setOnClickListener {
            val newName = binding.etPlaylistName.text.toString()
            if (!TextUtils.isEmpty(newName)) {
                if (!PlaylistsUtil.isPlaylistExists(newName)) {
                    PlaylistsUtil.removePlaylist(playlist.name)
                    PlaylistsUtil.addPlaylist(Playlist(newName, playlist.songs))
                    dismiss()
                } else {
                    Toast.makeText(context, R.string.toast_playlist_exists, Toast.LENGTH_LONG).show()
                }
            }
        }
        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.dialog_delete_playlist_title).setMessage(R.string.dialog_delete_playlist_content).setPositiveButton(R.string.dialog_yes) { _, _ ->
                PlaylistsUtil.removePlaylist(playlist.name)
                dismiss()
            }.setNegativeButton(R.string.dialog_no, null).show()
        }
        binding.btnCancel.setOnClickListener { dismiss() }
        isCancelable = true
        return binding.root
    }

}