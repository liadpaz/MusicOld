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
import com.liadpaz.amp.adapters.SearchSongListAdapter
import com.liadpaz.amp.databinding.FragmentSearchBinding
import com.liadpaz.amp.dialogs.PlaylistsDialog
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.viewmodels.Song

class SearchFragment private constructor(private val query: String, private val songs: List<Song>) : Fragment() {
    private val TAG = "AmpApp.SearchFragment"

    private lateinit var adapter: SearchSongListAdapter

    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentSearchBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvQuery.text = getString(R.string.search_result, query)
        adapter = SearchSongListAdapter(requireContext(), OnRecyclerItemClickListener { v: View?, position: Int ->
            PopupMenu(requireContext(), v!!) .apply {
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
        })
        binding.rvSearchSongs.adapter = adapter
        binding.rvSearchSongs.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.rvSearchSongs.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        adapter.submitList(songs)
    }

    companion object {
        @JvmStatic
        fun newInstance(query: String, songs: List<Song>): SearchFragment {
            return SearchFragment(query, songs)
        }
    }

}