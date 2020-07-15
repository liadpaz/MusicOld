package com.liadpaz.amp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentSearchBinding
import com.liadpaz.amp.view.adapters.SearchSongListAdapter
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.view.dialogs.PlaylistsDialog
import com.liadpaz.amp.viewmodels.SearchViewModel

class SearchFragment() : Fragment() {

    private lateinit var query: String
    private lateinit var songs: List<Song>

    private lateinit var adapter: SearchSongListAdapter

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModel.Factory(requireActivity().application, query)
    }
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        query = arguments?.getString("query")!!
        return FragmentSearchBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        songs = viewModel.getSongs().value!!.filter { song -> song.isMatchingQuery(query) }
        binding.tvQuery.text = getString(R.string.search_result, query)
        adapter = SearchSongListAdapter({ position ->
            viewModel.play(position.toLong())
        }) { v: View?, position: Int ->
            PopupMenu(requireContext(), v!!).apply {
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
                show()
            }
        }
        binding.rvSearchSongs.adapter = adapter
        binding.rvSearchSongs.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.rvSearchSongs.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        adapter.submitList(songs)
    }

    companion object {
        @JvmStatic
        fun newInstance(query: String): SearchFragment =
                SearchFragment().apply {
                    arguments = bundleOf(Pair("query", query))
                }
    }
}

private const val TAG = "AmpApp.SearchFragment"