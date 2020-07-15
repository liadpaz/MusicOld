package com.liadpaz.amp.view.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.DialogPlaylistsBinding
import com.liadpaz.amp.view.data.Playlist
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.viewmodels.PlaylistsViewModel
import java.util.*
import kotlin.collections.ArrayList

class PlaylistsDialog : DialogFragment() {

    private var song: Song? = null
    private var songsList: ArrayList<Song>? = null

    private val viewModel: PlaylistsViewModel by viewModels()
    private lateinit var binding: DialogPlaylistsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        song = arguments?.getParcelable("song")
        songsList = arguments?.getParcelableArrayList("songs")
        return DialogPlaylistsBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.playlistsObservable.observe(viewLifecycleOwner) {
            binding.spinnerPlaylists.adapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, ArrayList(it.keys))
        }

        binding.tvAddToPlaylist.text = song?.title

        binding.spinnerPlaylists.adapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, arrayListOf<String>())
        binding.btnNewPlaylist.setOnClickListener {
            if (songsList == null) {
                NewPlaylistDialog.newInstance(song).show(requireParentFragment().childFragmentManager, null)
            } else {
                NewPlaylistDialog.newInstance(songs = songsList!!).show(requireParentFragment().childFragmentManager, null)
            }
            dismiss()
        }
        binding.btnAdd.setOnClickListener {
            if (binding.spinnerPlaylists.selectedItem != null) {
                viewModel.addSongToPlaylist(binding.spinnerPlaylists.selectedItem as String, song, songsList)
                dismiss()
            }
        }
        binding.btnCancel.setOnClickListener { dismiss() }
        isCancelable = true
    }

    companion object {
        @JvmStatic
        fun newInstance(song: Song? = null, songs: List<Song>? = null): PlaylistsDialog =
                PlaylistsDialog().apply {
                    arguments = bundleOf(Pair("song", song), Pair("songs", songs))
                }
    }
}

private const val TAG = "AmpApp.PlaylistsDialog"