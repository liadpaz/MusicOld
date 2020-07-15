package com.liadpaz.amp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentArtistListBinding
import com.liadpaz.amp.view.adapters.ArtistsListAdapter
import com.liadpaz.amp.view.data.Artist
import com.liadpaz.amp.viewmodels.ArtistListViewModel

class ArtistListFragment : Fragment() {

    private var artists = listOf<Artist>()

    private lateinit var adapter: ArtistsListAdapter

    private val viewModel: ArtistListViewModel by viewModels()
    private lateinit var binding: FragmentArtistListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentArtistListBinding.inflate(layoutInflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.artists.observe(viewLifecycleOwner) {
            adapter.clear()
            adapter.addAll(it)
            artists = it
        }

        binding.lvArtists.adapter = ArtistsListAdapter(requireContext(), ArrayList(artists)).also { adapter = it }
        binding.lvArtists.onItemClickListener = OnItemClickListener { _, _, position: Int, _ ->
            requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFragment, ArtistSongListFragment.newInstance(artists[position]))
                    .addToBackStack(null)
                    .commit()
        }
    }

    companion object {
        private const val TAG = "AmpApp.ArtistListFragment"

        @JvmStatic
        fun newInstance(): ArtistListFragment =
                ArtistListFragment()
    }
}