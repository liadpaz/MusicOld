package com.liadpaz.amp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentAlbumSongListBinding
import com.liadpaz.amp.ui.fragments.SongsListFragment.Companion.newInstance
import com.liadpaz.amp.ui.viewmodels.Album

class AlbumSongListFragment : Fragment() {

    private lateinit var album: Album

    private lateinit var binding: FragmentAlbumSongListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        album = arguments?.getParcelable("album")!!
        return FragmentAlbumSongListBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.album = album
        childFragmentManager.beginTransaction().replace(R.id.containerFragment, newInstance(album.songs)).commit()
    }

    companion object {
        private const val TAG = "AmpApp.AlbumSongListFragment"

        @JvmStatic
        fun newInstance(album: Album): AlbumSongListFragment {
            return AlbumSongListFragment().apply {
                arguments = bundleOf(Pair("album", album))
            }
        }
    }
}