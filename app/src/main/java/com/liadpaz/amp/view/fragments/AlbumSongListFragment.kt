package com.liadpaz.amp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.databinding.FragmentAlbumSongListBinding
import com.liadpaz.amp.service.server.service.ServiceConnection
import com.liadpaz.amp.view.adapters.SongsListAdapter
import com.liadpaz.amp.view.data.Album
import com.liadpaz.amp.view.views.SongPopUpMenu
import com.liadpaz.amp.viewmodels.livedatautils.QueueUtil
import java.util.*

class AlbumSongListFragment : Fragment() {

    private lateinit var album: Album

    private lateinit var adapter: SongsListAdapter

    private lateinit var binding: FragmentAlbumSongListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        album = arguments?.getParcelable("album")!!
        return FragmentAlbumSongListBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.album = album

        adapter = SongsListAdapter(requireContext(), { v: View?, position: Int ->
            SongPopUpMenu(this, v!!, adapter.currentList[position]).show()
        }, View.OnClickListener {
            QueueUtil.queue.postValue(ArrayList(adapter.currentList).apply { shuffle() })
            QueueUtil.queuePosition.postValue(0)
            ServiceConnection.playFromQueue()
        }).also { adapter ->
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