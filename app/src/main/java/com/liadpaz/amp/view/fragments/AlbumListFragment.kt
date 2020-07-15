package com.liadpaz.amp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentAlbumListBinding
import com.liadpaz.amp.view.adapters.AlbumsListAdapter
import com.liadpaz.amp.view.data.Album
import com.liadpaz.amp.viewmodels.AlbumListViewModel

class AlbumListFragment : Fragment() {

    private var albums = listOf<Album>()

    private lateinit var adapter: AlbumsListAdapter

    private val viewModel: AlbumListViewModel by viewModels()
    private lateinit var binding: FragmentAlbumListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentAlbumListBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.albums.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            albums = it
        }

        adapter = AlbumsListAdapter() { _, position: Int ->
            requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFragment, AlbumSongListFragment.newInstance(albums[position]))
                    .addToBackStack(null)
                    .commit()
        }

        binding.rvAlbums.adapter = adapter
    }

    companion object {
        private const val TAG = "AmpApp.AlbumListFragment"

        @JvmStatic
        fun newInstance(): AlbumListFragment =
                AlbumListFragment()
    }
}