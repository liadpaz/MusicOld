package com.liadpaz.amp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentAlbumSongListBinding
import com.liadpaz.amp.fragments.SongsListFragment.Companion.newInstance
import com.liadpaz.amp.viewmodels.Album

class AlbumSongListFragment private constructor(private val album: Album) : Fragment() {
    private val TAG = "AmpApp.AlbumSongListFragment"

    private lateinit var binding: FragmentAlbumSongListBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentAlbumSongListBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.album = album
        childFragmentManager.beginTransaction().replace(R.id.containerFragment, newInstance(album.songs)).commit()
    }

    companion object {
        fun newInstance(album: Album): AlbumSongListFragment {
            return AlbumSongListFragment(album)
        }
    }
}