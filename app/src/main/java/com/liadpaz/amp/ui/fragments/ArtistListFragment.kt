package com.liadpaz.amp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.liadpaz.amp.R
import com.liadpaz.amp.ui.adapters.ArtistsListAdapter
import com.liadpaz.amp.databinding.FragmentArtistListBinding
import com.liadpaz.amp.livedatautils.SongsUtil
import com.liadpaz.amp.ui.viewmodels.Artist
import com.liadpaz.amp.ui.viewmodels.Song
import java.util.*

class ArtistListFragment : Fragment() {

    private val artists = ArrayList<Artist>()

    private lateinit var adapter: ArtistsListAdapter

    private lateinit var binding: FragmentArtistListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentArtistListBinding.inflate(layoutInflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lvArtists.adapter = ArtistsListAdapter(requireContext(), ArrayList(artists)).also { adapter = it }
        binding.lvArtists.onItemClickListener = OnItemClickListener { _, _, position: Int, _ -> requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.mainFragment, ArtistSongListFragment.newInstance(artists[position]))
                .addToBackStack(null)
                .commit() }
        SongsUtil.observeArtists(this, Observer { artistsList: HashMap<String, ArrayList<Song>> ->
            artists.clear()
            artistsList.forEach { (name: String, artistSongs: ArrayList<Song>?) -> artists.add(Artist(name, artistSongs)) }
            artists.sortBy { artist -> artist.name.toLowerCase(Locale.US) }
            adapter.clear()
            adapter.addAll(artists)
        })
    }

    companion object {
        private const val TAG = "AmpApp.ArtistListFragment"

        @JvmStatic
        fun newInstance(): ArtistListFragment {
            return ArtistListFragment()
        }
    }
}