package com.liadpaz.amp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentPlaylistsBinding
import com.liadpaz.amp.livedatautils.PlaylistsUtil
import com.liadpaz.amp.ui.adapters.PlaylistsAdapter
import com.liadpaz.amp.ui.dialogs.EditPlaylistDialog
import com.liadpaz.amp.ui.dialogs.NewPlaylistDialog
import com.liadpaz.amp.ui.viewmodels.Playlist
import com.liadpaz.amp.ui.viewmodels.Song
import com.liadpaz.amp.utils.LocalFiles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.*
import kotlin.collections.ArrayList

class PlaylistsFragment : Fragment() {
    private lateinit var recentlyAddedPlaylist: Playlist
    private lateinit var playlists: ArrayList<Playlist>

    private lateinit var binding: FragmentPlaylistsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentPlaylistsBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        CoroutineScope(Dispatchers.Main).async {
            recentlyAddedPlaylist = Playlist(getString(R.string.playlist_recently_added), LocalFiles.listSongsByLastAdded(requireContext().contentResolver))
            playlists = ArrayList<Playlist>().apply {
                add(recentlyAddedPlaylist)
            }
        }.invokeOnCompletion {
            PlaylistsAdapter(requireContext(), { _, position: Int ->
                requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.mainFragment, PlaylistFragment.newInstance(playlists[position]))
                        .addToBackStack(null)
                        .commit()
            }, { position: Int ->
                if (position != 0) {
                    EditPlaylistDialog(playlists[position]).show(childFragmentManager, null)
                }
                true
            }).also { adapter ->
                PlaylistsUtil.observe(requireActivity(), Observer { playlists: Queue<Playlist>? ->
                    playlists?.let {
                        this.playlists = ArrayList(playlists).apply { add(0, recentlyAddedPlaylist) }
                        LocalFiles.setPlaylists(playlists)
                        adapter.submitList(this.playlists)
                    }
                })

                binding.rvPlaylists.adapter = adapter
                adapter.submitList(playlists)
            }
        }

        binding.rvPlaylists.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlaylists.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
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