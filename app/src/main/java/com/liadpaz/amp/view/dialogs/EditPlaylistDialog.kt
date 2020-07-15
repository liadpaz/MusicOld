package com.liadpaz.amp.view.dialogs

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.DialogEditPlaylistBinding
import com.liadpaz.amp.view.data.Playlist
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.viewmodels.PlaylistsViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class EditPlaylistDialog : DialogFragment() {

    private lateinit var playlistName: String

    private var playlists: LinkedHashMap<String, ArrayList<Song>> = LinkedHashMap()

    private val viewModel: PlaylistsViewModel by viewModels()
    private lateinit var binding: DialogEditPlaylistBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        playlistName = arguments?.getString("playlist")!!
        return DialogEditPlaylistBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.playlistsObservable.observe(viewLifecycleOwner) {
            playlists = it
        }

        binding.etPlaylistName.setText(playlistName)
        // button to edit the playlist
        binding.btnApply.setOnClickListener {
            val newName = binding.etPlaylistName.text.toString()
            if (!TextUtils.isEmpty(newName)) {
                if (!playlists.containsKey(newName)) {
                    viewModel.renamePlaylist(playlistName, newName)
                    dismiss()
                } else {
                    Toast.makeText(context, R.string.toast_playlist_exists, Toast.LENGTH_LONG).show()
                }
            }
        }
        // button to delete playlist
        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.dialog_delete_playlist_title).setMessage(R.string.dialog_delete_playlist_content).setPositiveButton(R.string.dialog_yes) { _, _ ->
                viewModel.deletePlaylist(playlistName)
                dismiss()
            }.setNegativeButton(R.string.dialog_no, null).show()
        }
        binding.btnCancel.setOnClickListener { dismiss() }
        isCancelable = true
    }

    companion object {
        @JvmStatic
        fun newInstance(playlist: Playlist): EditPlaylistDialog =
                EditPlaylistDialog().apply {
                    arguments = bundleOf(Pair("playlist", playlist.name))
                }
    }
}

private const val TAG = "AmpApp.EditDialog"