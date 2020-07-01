package com.liadpaz.amp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.R
import com.liadpaz.amp.adapters.SongsListAdapter
import com.liadpaz.amp.databinding.FragmentSongsListBinding
import com.liadpaz.amp.dialogs.PlaylistsDialog
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.viewmodels.Song
import java.util.*

class SongsListFragment(private val songs: List<Song>) : Fragment() {
    private val TAG = "AmpApp.SongsListFragment"

    private lateinit var adapter: SongsListAdapter
    private lateinit var binding: FragmentSongsListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentSongsListBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SongsListAdapter(requireContext(), OnRecyclerItemClickListener { v: View?, position: Int ->
            PopupMenu(requireContext(), v!!).apply {
                inflate(R.menu.menu_song)
                setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menuPlayNext -> {
                            // TODO: play next
                        }
                        R.id.menuAddQueue -> {
                            // TODO: add to queue
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
        @JvmStatic
        fun newInstance(songs: List<Song>): SongsListFragment {
            return SongsListFragment(songs)
        }
    }

}