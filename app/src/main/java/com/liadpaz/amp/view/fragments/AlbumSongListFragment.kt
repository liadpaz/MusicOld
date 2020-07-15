package com.liadpaz.amp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.databinding.FragmentAlbumSongListBinding
import com.liadpaz.amp.view.adapters.SongsListAdapter
import com.liadpaz.amp.view.data.Album
import com.liadpaz.amp.view.views.SongPopUpMenu
import com.liadpaz.amp.viewmodels.AlbumViewModel

class AlbumSongListFragment : Fragment() {

    private lateinit var album: Album

    private lateinit var adapter: SongsListAdapter

    private val viewModel: AlbumViewModel by viewModels {
        AlbumViewModel.Factory(requireActivity().application, album.name)
    }
    private lateinit var binding: FragmentAlbumSongListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        album = arguments?.getParcelable("album")!!
        return FragmentAlbumSongListBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.album = album

        adapter = SongsListAdapter({ position ->
            viewModel.play(position)
        }, { v: View?, position: Int ->
            SongPopUpMenu(this, v!!, adapter.currentList[position]).show()
        }, { viewModel.playShuffle() }).also { adapter ->
            binding.rvSongs.adapter = adapter
            adapter.submitList(album.songs)
        }

        binding.rvSongs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0 && (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() == 0) {
                    binding.fabScrollUp.visibility = View.GONE
                } else if (binding.fabScrollUp.visibility != View.VISIBLE) {
                    binding.fabScrollUp.visibility = View.VISIBLE
                }
            }
        })
        binding.fabScrollUp.setOnClickListener { v: View ->
            binding.rvSongs.scrollToPosition(0)
            v.visibility = View.GONE
        }
    }

    companion object {
        private const val TAG = "AmpApp.AlbumSongListFragment"

        @JvmStatic
        fun newInstance(album: Album): AlbumSongListFragment =
                AlbumSongListFragment().apply {
                    arguments = bundleOf(Pair("album", album))
                }
    }
}