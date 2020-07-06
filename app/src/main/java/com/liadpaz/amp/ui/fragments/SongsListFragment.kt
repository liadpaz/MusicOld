package com.liadpaz.amp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.R
import com.liadpaz.amp.ui.adapters.SongsListAdapter
import com.liadpaz.amp.databinding.FragmentSongsListBinding
import com.liadpaz.amp.ui.dialogs.PlaylistsDialog
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.server.service.ServiceConnector
import com.liadpaz.amp.ui.viewmodels.Song
import java.util.*

class SongsListFragment : Fragment() {

    private lateinit var songs: List<Song>

    private lateinit var adapter: SongsListAdapter
    private lateinit var binding: FragmentSongsListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        songs = arguments?.getParcelableArrayList("songs")!!
        return FragmentSongsListBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SongsListAdapter(requireContext(), { v: View?, position: Int ->
            PopupMenu(requireContext(), v!!).apply {
                inflate(R.menu.menu_song)
                setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menuPlayNext -> {
                            ServiceConnector.getInstance().mediaSource.value?.addMediaSource(QueueUtil.queuePosition.value!! + 1, songs[position].toMediaSource(requireContext()))
                        }
                        R.id.menuAddQueue -> {
                            ServiceConnector.getInstance().mediaSource.value?.addMediaSource(songs[position].toMediaSource(requireContext()))
                        }
                        R.id.menuAddToPlaylist -> {
                            PlaylistsDialog(adapter.currentList[position]).show(childFragmentManager, null)
                        }
                    }
                    true
                }
                show()
            }
        }, View.OnClickListener {
            QueueUtil.queue.postValue(ArrayList(adapter.currentList).apply { shuffle() })
            QueueUtil.queuePosition.postValue(0)
            ServiceConnector.playFromQueue()
        }).apply {
            submitList(songs)
            binding.rvSongs.adapter = this
        }
        binding.rvSongs.layoutManager = LinearLayoutManager(requireContext()).apply {
            isSmoothScrollbarEnabled = true
        }
        binding.rvSongs.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.rvSongs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0 && (recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition() == 0) {
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
        private const val TAG = "AmpApp.SongsListFragment"

        @JvmStatic
        fun newInstance(songs: List<Song>): SongsListFragment {
            return SongsListFragment().apply {
                arguments = bundleOf(Pair("songs", songs))
            }
        }
    }
}