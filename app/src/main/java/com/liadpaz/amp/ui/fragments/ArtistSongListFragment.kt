package com.liadpaz.amp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentArtistSongListBinding
import com.liadpaz.amp.ui.fragments.SongsListFragment.Companion.newInstance
import com.liadpaz.amp.ui.viewmodels.Artist

class ArtistSongListFragment : Fragment() {

    private lateinit var artist: Artist

    private lateinit var binding: FragmentArtistSongListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        artist = arguments?.getParcelable("artist")!!
        return FragmentArtistSongListBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.artist = artist
        childFragmentManager.beginTransaction().replace(R.id.containerFragment, newInstance(artist.songs)).commit()
    }

    companion object {
        private const val TAG = "AmpApp.ArtistSongListFragment"

        @JvmStatic
        fun newInstance(artist: Artist): ArtistSongListFragment {
            return ArtistSongListFragment().apply {
                arguments = bundleOf(Pair("artist", artist))
            }
        }
    }
}