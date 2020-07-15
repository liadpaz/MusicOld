package com.liadpaz.amp.view.dialogs

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.DialogNewPlaylistBinding
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.viewmodels.PlaylistsViewModel

class NewPlaylistDialog : DialogFragment() {

    private var song: Song? = null
    private var songs: ArrayList<Song>? = null

    private var playlists: LinkedHashMap<String, ArrayList<Song>> = LinkedHashMap()

    private val viewModel: PlaylistsViewModel by viewModels()
    private lateinit var binding: DialogNewPlaylistBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        song = arguments?.getParcelable("song")
        songs = arguments?.getParcelableArrayList("songs")
        return DialogNewPlaylistBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.playlistsObservable.observe(viewLifecycleOwner) {
            playlists = it
        }

        binding.btnCreate.setOnClickListener {
            val name = binding.etPlaylistName.text.toString()
            if (!TextUtils.isEmpty(name)) {
                if (!playlists.containsKey(name)) {
                    viewModel.addPlaylist(name, song?.let { arrayListOf(it) } ?: songs)
                    dismiss()
                } else {
                    Toast.makeText(context, R.string.toast_playlist_exists, Toast.LENGTH_LONG).show()
                }
            }
        }
        binding.btnCancel.setOnClickListener { dismiss() }
        isCancelable = true
    }

    companion object {

        @JvmStatic
        fun newInstance(song: Song? = null, songs: List<Song>? = null): NewPlaylistDialog =
                NewPlaylistDialog().apply {
                    arguments = bundleOf(Pair("song", song), Pair("songs", songs))

                }
    }
}