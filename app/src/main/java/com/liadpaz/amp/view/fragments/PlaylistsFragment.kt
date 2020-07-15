package com.liadpaz.amp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentPlaylistsBinding
import com.liadpaz.amp.view.adapters.PlaylistsAdapter
import com.liadpaz.amp.view.data.Playlist
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.view.dialogs.EditPlaylistDialog
import com.liadpaz.amp.view.dialogs.NewPlaylistDialog
import com.liadpaz.amp.viewmodels.PlaylistsViewModel
import java.util.ArrayList

class PlaylistsFragment : Fragment() {

    private lateinit var recentlyAddedPlaylist: Playlist
    private var playlists = arrayListOf<Playlist>()

    private lateinit var adapter: PlaylistsAdapter

    private val viewModel: PlaylistsViewModel by viewModels()
    private lateinit var binding: FragmentPlaylistsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentPlaylistsBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recentlyAddedPlaylist = Playlist(getString(R.string.playlist_recently_added), arrayListOf())
        viewModel.recentlyAddedPlaylist.observe(viewLifecycleOwner) {
            recentlyAddedPlaylist = Playlist(getString(R.string.playlist_recently_added), it)
            adapter.submitList(arrayListOf(recentlyAddedPlaylist) + playlists)
        }
        viewModel.playlistsObservable.observe(viewLifecycleOwner) {
            playlists = ArrayList(it.map { entry: Map.Entry<String, ArrayList<Song>> -> Playlist(entry.key, entry.value) })
            adapter.submitList(arrayListOf(recentlyAddedPlaylist) + playlists)
        }
        adapter = PlaylistsAdapter({ position: Int ->
            requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFragment, PlaylistFragment.newInstance(if (position == 0) recentlyAddedPlaylist else playlists[position - 1]))
                    .addToBackStack(null)
                    .commit()
        }, { position: Int ->
            if (position != 0) {
                EditPlaylistDialog.newInstance(playlists[position - 1]).show(childFragmentManager, null)
            }
            true
        }).also {
            binding.rvPlaylists.adapter = it
        }

        binding.fabNewPlaylist.setOnClickListener { NewPlaylistDialog.newInstance().show(childFragmentManager, null) }
    }

    companion object {
        private const val TAG = "AmpApp.PlaylistsFragment"

        @JvmStatic
        fun newInstance(): PlaylistsFragment =
                PlaylistsFragment()
    }
}
