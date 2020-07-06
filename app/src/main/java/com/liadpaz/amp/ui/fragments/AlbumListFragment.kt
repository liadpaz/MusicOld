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
import com.liadpaz.amp.ui.adapters.AlbumsListAdapter
import com.liadpaz.amp.databinding.FragmentAlbumListBinding
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.livedatautils.SongsUtil
import com.liadpaz.amp.ui.viewmodels.Album
import com.liadpaz.amp.ui.viewmodels.Song
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AlbumListFragment : Fragment() {
    private val albums = ArrayList<Album>()
    private lateinit var binding: FragmentAlbumListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentAlbumListBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = AlbumsListAdapter(requireContext()) { _: View?, position: Int ->
            requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFragment, AlbumSongListFragment.newInstance(albums[position]))
                    .addToBackStack(null)
                    .commit()
        }
        binding.rvAlbums.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlbums.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.rvAlbums.adapter = adapter
        SongsUtil.observeAlbums(viewLifecycleOwner, Observer { albumsList: HashMap<String, ArrayList<Song>> ->
            albums.clear()
            albumsList.forEach { (name: String?, albumSongs: ArrayList<Song>?) -> albums.add(Album(name, albumSongs[0].artists[0], albumSongs)) }
            albums.sortBy { album -> album.name.toLowerCase(Locale.US) }
            adapter.submitList(albums)
        })
    }

    companion object {
        private const val TAG = "AmpApp.AlbumListFragment"

        @JvmStatic
        fun newInstance(): AlbumListFragment {
            return AlbumListFragment()
        }
    }
}