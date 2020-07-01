package com.liadpaz.amp.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.DialogPlaylistsBinding
import com.liadpaz.amp.livedatautils.PlaylistsUtil
import com.liadpaz.amp.viewmodels.Song
import java.util.*

class PlaylistsDialog : DialogFragment {
    private var song: Song? = null
    private var songs: ArrayList<Song>? = null

    constructor(song: Song) {
        this.song = song
    }

    constructor(songs: ArrayList<Song>) {
        this.songs = songs
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DialogPlaylistsBinding.inflate(inflater, container, false)
        if (song != null) {
            binding.tvAddToPlaylist.text = song!!.title
        }
        binding.spinnerPlaylists.adapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, PlaylistsUtil.playlistsNames)
        binding.btnNewPlaylist.setOnClickListener {
            if (songs == null) {
                NewPlaylistDialog(song).show(requireParentFragment().childFragmentManager, null)
            } else {
                NewPlaylistDialog(songs!!).show(requireParentFragment().childFragmentManager, null)
            }
            dismiss()
        }
        binding.btnAdd.setOnClickListener {
            if (binding.spinnerPlaylists.selectedItem != null) {
                PlaylistsUtil.addPlaylist(PlaylistsUtil.removePlaylist((binding.spinnerPlaylists.selectedItem as String)).also {
                    song?.let { songs?.add(it) }
                            ?: this.songs?.let { songs?.addAll(it) }
                }!!)
                dismiss()
            }
        }
        binding.btnCancel.setOnClickListener { dismiss() }
        isCancelable = true
        return binding.root
    }
}