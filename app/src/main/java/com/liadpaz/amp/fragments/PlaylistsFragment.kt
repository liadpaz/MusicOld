package com.liadpaz.amp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.liadpaz.amp.R
import com.liadpaz.amp.adapters.PlaylistsAdapter
import com.liadpaz.amp.databinding.FragmentPlaylistsBinding
import com.liadpaz.amp.dialogs.EditPlaylistDialog
import com.liadpaz.amp.dialogs.NewPlaylistDialog
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.livedatautils.PlaylistsUtil
import com.liadpaz.amp.utils.LocalFiles
import com.liadpaz.amp.viewmodels.Playlist
import com.liadpaz.amp.viewmodels.Song
import java.util.*
import java.util.function.Function
import kotlin.collections.ArrayList

class PlaylistsFragment : Fragment() {
    private lateinit var recentlyAddedPlaylist: Playlist
    private lateinit var playlists: ArrayList<Playlist>

    private lateinit var adapter: PlaylistsAdapter

    private lateinit var binding: FragmentPlaylistsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentPlaylistsBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recentlyAddedPlaylist = Playlist(getString(R.string.playlist_recently_added), LocalFiles.listSongsByLastAdded(requireContext().contentResolver))
        playlists = ArrayList<Playlist>().apply {
            add(recentlyAddedPlaylist)
        }
        adapter = PlaylistsAdapter(requireContext(), OnRecyclerItemClickListener { _, position: Int ->
            requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFragment, PlaylistFragment.newInstance(playlists[position]))
                    .addToBackStack(null)
                    .commit()
        }, Function { position: Int ->
            if (position != 0) {
                EditPlaylistDialog(playlists[position]).show(childFragmentManager, null)
            }
            true
        })

        PlaylistsUtil.observe(requireActivity(), Observer { playlistQueue: Queue<Playlist>? ->
            if (playlistQueue != null) {
                playlists = ArrayList(playlistQueue).apply { add(0, recentlyAddedPlaylist) }
                LocalFiles.setPlaylists(playlistQueue)
                adapter.submitList(playlists)
            }
        })

        binding.rvPlaylists.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlaylists.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.rvPlaylists.adapter = adapter
        adapter.submitList(playlists)
        binding.fabNewPlaylist.setOnClickListener { NewPlaylistDialog(null as Song?).show(childFragmentManager, null) }
    }

    companion object {
        private const val TAG = "AmpApp.PlaylistsFragment"

        @JvmStatic
        fun newInstance(): PlaylistsFragment {
            return PlaylistsFragment()
        }
    }
}