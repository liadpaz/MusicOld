package com.liadpaz.amp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentPlaylistBinding
import com.liadpaz.amp.view.adapters.PlaylistAdapter
import com.liadpaz.amp.view.adapters.SongsListAdapter
import com.liadpaz.amp.view.data.Playlist
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.view.dialogs.PlaylistsDialog
import com.liadpaz.amp.view.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.viewmodels.PlaylistViewModel

class PlaylistFragment : Fragment() {

    private var isChanging = false

    private lateinit var playlist: String

    private lateinit var adapter: ListAdapter<Song, out RecyclerView.ViewHolder>

    private val viewModel: PlaylistViewModel by viewModels {
        PlaylistViewModel.Factory(requireActivity().application, playlist)
    }
    private lateinit var binding: FragmentPlaylistBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentPlaylistBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        playlist = arguments?.getString("playlist")!!

        viewModel.getPlaylist().observe(viewLifecycleOwner) {
            Log.d(TAG, "$it")
            if (isChanging) {
                isChanging = false
            } else {
                adapter.submitList(it)
            }
        }
        binding.tvPlaylistName.text = playlist

        val onMoreClicked: OnRecyclerItemClickListener = { v: View, position: Int ->
            PopupMenu(requireContext(), v).apply {
                inflate(if (playlist == getString(R.string.playlist_recently_added)) R.menu.menu_playlist_recently else R.menu.menu_playlist)
                setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menuPlayNext -> {
                            viewModel.addToNext(adapter.currentList[position])
                        }
                        R.id.menuAddQueue -> {
                            viewModel.addToQueue(adapter.currentList[position])
                        }
                        R.id.menuQueueAddPlaylist -> {
                            PlaylistsDialog.newInstance(adapter.currentList[position]).show(childFragmentManager, null)
                        }
                        R.id.menuRemoveFromPlaylist -> {
                            viewModel.removeSong(position)
                        }
                    }
                    true
                }
            }.show()
        }
        val onShuffleClickListener = {
            if (adapter.currentList.size != 0) {
                viewModel.playShuffle()
            }
        }
        if (playlist == getString(R.string.playlist_recently_added)) {
            adapter = SongsListAdapter({}, onMoreClicked, onShuffleClickListener)
            binding.tvShuffle.visibility = View.GONE
            binding.btnDelete.visibility = View.INVISIBLE
        } else {
            adapter = PlaylistAdapter({ position ->
                viewModel.play(position)
            }, onMoreClicked)
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(DOWN or UP, 0) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

                override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
                    super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
                    isChanging = true
                    viewModel.moveSongPosition(fromPos, toPos)
                }
            }).apply {
                attachToRecyclerView(binding.rvSongs)
                (adapter as PlaylistAdapter).setOnStartDragListener { viewHolder -> startDrag(viewHolder) }
            }

            binding.tvShuffle.setOnClickListener { onShuffleClickListener() }
            binding.btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.dialog_delete_playlist_title).setMessage(R.string.dialog_delete_playlist_content).setPositiveButton(R.string.dialog_yes) { _, _ ->
                    viewModel.deletePlaylist()
                    parentFragmentManager.popBackStack()
                }.setNegativeButton(R.string.dialog_no, null).show()
            }
        }
        binding.rvSongs.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance(playlist: Playlist): PlaylistFragment = PlaylistFragment().apply {
            arguments = bundleOf(Pair("playlist", playlist.name))
        }
    }
}

private const val TAG = "AmpApp.PlaylistFragment"