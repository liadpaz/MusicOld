package com.liadpaz.amp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.databinding.FragmentSongsListBinding
import com.liadpaz.amp.service.server.service.ServiceConnection
import com.liadpaz.amp.view.adapters.SongsListAdapter
import com.liadpaz.amp.view.views.SongPopUpMenu
import com.liadpaz.amp.viewmodels.SongListViewModel
import com.liadpaz.amp.viewmodels.livedatautils.QueueUtil
import java.util.*

class SongsListFragment : Fragment() {

    private lateinit var adapter: SongsListAdapter

    private val viewModel: SongListViewModel by viewModels()
    private lateinit var binding: FragmentSongsListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentSongsListBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.songsObservable.observe(viewLifecycleOwner) { adapter.submitList(it) }

        adapter = SongsListAdapter(requireContext(), { v: View?, position: Int ->
            SongPopUpMenu(this, v!!, adapter.currentList[position]).show()
        }, View.OnClickListener {
            QueueUtil.queue.postValue(ArrayList(adapter.currentList).apply { shuffle() })
            QueueUtil.queuePosition.postValue(0)
            ServiceConnection.playFromQueue()
        }).also { adapter ->
            binding.rvSongs.adapter = adapter
        }

        binding.rvSongs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0 && (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() == 0) {
                    binding.fabScrollUp.visibility = View.GONE
                } else if (binding.fabScrollUp.visibility != View.VISIBLE) {
                    binding.fabScrollUp.visibility = View.VISIBLE
                }
            }
        })
        binding.fabScrollUp.setOnClickListener { v: View ->
            binding.rvSongs.scrollToPosition(0)
            v.visibility = View.GONE
        }
    }

    companion object {
        private const val TAG = "AmpApp.SongsListFragment"

        @JvmStatic
        fun newInstance(): SongsListFragment = SongsListFragment()
    }
}