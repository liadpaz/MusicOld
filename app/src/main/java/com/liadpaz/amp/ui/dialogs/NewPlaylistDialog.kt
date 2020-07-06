package com.liadpaz.amp.ui.dialogs

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.DialogNewPlaylistBinding
import com.liadpaz.amp.livedatautils.PlaylistsUtil
import com.liadpaz.amp.ui.viewmodels.Playlist
import com.liadpaz.amp.ui.viewmodels.Song
import java.util.*

class NewPlaylistDialog : DialogFragment {
    private var song: Song? = null
    private var songs: ArrayList<Song>? = null

    constructor(song: Song?) {
        this.song = song
    }

    constructor(songs: ArrayList<Song>) {
        this.songs = songs
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DialogNewPlaylistBinding.inflate(inflater, container, false)
        binding.btnCreate.setOnClickListener {
            val name = binding.etPlaylistName.text.toString()
            if (!TextUtils.isEmpty(name)) {
                if (!PlaylistsUtil.isPlaylistExists(name)) {
                    PlaylistsUtil.addPlaylist(Playlist(name, ArrayList<Song>().apply {
                        song?.also { add(it) } ?: songs?.let { addAll(it) }
                    }))
                    dismiss()
                } else {
                    Toast.makeText(context, R.string.toast_playlist_exists, Toast.LENGTH_LONG).show()
                }
            }
        }
        binding.btnCancel.setOnClickListener { dismiss() }
        isCancelable = true
        return binding.root
    }
}