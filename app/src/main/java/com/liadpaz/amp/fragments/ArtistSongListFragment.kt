package com.liadpaz.amp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentArtistSongListBinding
import com.liadpaz.amp.fragments.SongsListFragment.Companion.newInstance
import com.liadpaz.amp.viewmodels.Artist

class ArtistSongListFragment private constructor(private val artist: Artist) : Fragment() {
    private val TAG = "AmpApp.ArtistSongListFragment"

    private lateinit var binding: FragmentArtistSongListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentArtistSongListBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.artist = artist
        childFragmentManager.beginTransaction().replace(R.id.containerFragment, newInstance(artist.songs)).commit()
    }

    companion object {
        fun newInstance(artist: Artist): ArtistSongListFragment {
            return ArtistSongListFragment(artist)
        }
    }

}