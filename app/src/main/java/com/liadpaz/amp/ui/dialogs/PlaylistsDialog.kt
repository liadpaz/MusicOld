package com.liadpaz.amp.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.DialogPlaylistsBinding
import com.liadpaz.amp.livedatautils.PlaylistsUtil
import com.liadpaz.amp.ui.viewmodels.Song
import java.util.*

class PlaylistsDialog : DialogFragment {
    private var song: Song? = null
    private var songsList: ArrayList<Song>? = null

    constructor(song: Song) {
        this.song = song
    }

    constructor(songs: ArrayList<Song>) {
        this.songsList = songs
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DialogPlaylistsBinding.inflate(inflater, container, false)

        binding.tvAddToPlaylist.text = song?.title

        binding.spinnerPlaylists.adapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, PlaylistsUtil.playlistsNames)
        binding.btnNewPlaylist.setOnClickListener {
            if (songsList == null) {
                NewPlaylistDialog(song).show(requireParentFragment().childFragmentManager, null)
            } else {
                NewPlaylistDialog(songsList!!).show(requireParentFragment().childFragmentManager, null)
            }
            dismiss()
        }
        binding.btnAdd.setOnClickListener {
            if (binding.spinnerPlaylists.selectedItem != null) {
                PlaylistsUtil.addPlaylist(PlaylistsUtil.removePlaylist(binding.spinnerPlaylists.selectedItem as String).apply {
                    song?.let { this?.songs?.add(song!!) } ?: songsList?.let { this?.songs?.addAll(it) }
                }!!)
                dismiss()
            }
        }
        binding.btnCancel.setOnClickListener { dismiss() }
        isCancelable = true
        return binding.root
    }
}