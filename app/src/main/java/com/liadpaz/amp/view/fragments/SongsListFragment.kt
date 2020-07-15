package com.liadpaz.amp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentSongsListBinding
import com.liadpaz.amp.server.utils.buildMediaId
import com.liadpaz.amp.view.adapters.SongsListAdapter
import com.liadpaz.amp.view.dialogs.PlaylistsDialog
import com.liadpaz.amp.viewmodels.SongListViewModel

class SongsListFragment : Fragment() {

    private lateinit var adapter: SongsListAdapter

    private val viewModel: SongListViewModel by viewModels()
    private lateinit var binding: FragmentSongsListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentSongsListBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.getSongs().observe(viewLifecycleOwner) { adapter.submitList(it) }

        adapter = SongsListAdapter({ position ->
            viewModel.transportControls.playFromMediaId(buildMediaId("all", null, position), null)
        }, { v: View, position: Int ->
            PopupMenu(requireContext(), v).apply {
                inflate(R.menu.menu_song)
                setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menuPlayNext -> {
                            viewModel.addToNext(adapter.currentList[position])
                        }
                        R.id.menuAddQueue -> {
                            viewModel.addToQueue(adapter.currentList[position])
                        }
                        R.id.menuAddToPlaylist -> {
                            PlaylistsDialog.newInstance(adapter.currentList[position]).show(childFragmentManager, null)
                        }
                    }
                    true
                }
            }.show()
        }, { viewModel.playShuffle() }).also { adapter ->
            binding.rvSongs.adapter = adapter
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
        private const val TAG = "AmpApp.SongsListFragment"

        @JvmStatic
        fun newInstance(): SongsListFragment = SongsListFragment()
    }
}