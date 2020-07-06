package com.liadpaz.amp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ItemTouchHelper.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentPlaylistBinding
import com.liadpaz.amp.interfaces.ItemTouchHelperAdapter
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.livedatautils.PlaylistsUtil
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.server.service.ServiceConnector
import com.liadpaz.amp.ui.adapters.PlaylistAdapter
import com.liadpaz.amp.ui.adapters.SongsListAdapter
import com.liadpaz.amp.ui.dialogs.PlaylistsDialog
import com.liadpaz.amp.ui.viewmodels.Playlist
import com.liadpaz.amp.ui.viewmodels.Song
import java.util.*

class PlaylistFragment(private val playlist: Playlist) : Fragment() {

    private lateinit var adapter: ListAdapter<Song, out RecyclerView.ViewHolder>

    private lateinit var binding: FragmentPlaylistBinding

    override fun onCreateView(inflater: LayoutInflater, container:
    ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentPlaylistBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val onMoreClicked: OnRecyclerItemClickListener = { v: View, position: Int ->
            PopupMenu(requireContext(), v).apply {
                inflate(if (playlist.name == getString(R.string.playlist_recently_added)) R.menu.menu_playlist_recently else R.menu.menu_playlist)
                setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menuPlayNext -> {
                            ServiceConnector.getInstance().mediaSource.value!!.addMediaSource(QueueUtil.queuePosition.value!! + 1, adapter.currentList[position].toMediaSource(requireContext()))
                        }
                        R.id.menuAddQueue -> {
                            ServiceConnector.getInstance().mediaSource.value!!.addMediaSource(adapter.currentList[position].toMediaSource(requireContext()))
                        }
                        R.id.menuQueueAddPlaylist -> {
                            PlaylistsDialog(adapter.currentList[position]).show(childFragmentManager, null)
                        }
                        R.id.menuRemoveFromPlaylist -> {
                            PlaylistsUtil.removePlaylist(playlist.name)?.also {
                                playlist.songs.removeAt(position)
                                adapter.submitList(playlist.songs)  //notifyItemRemoved(position + 1)
                                PlaylistsUtil.setIsChanging(true)
                                PlaylistsUtil.addPlaylist(it)
                            }
                        }
                    }
                    true
                }
                show()
            }
        }
        val onShuffleClickListener = View.OnClickListener {
            if (playlist.songs.size != 0) {
                val queue = ArrayList(playlist.songs)
                queue.shuffle()
                QueueUtil.queue.postValue(queue)
                QueueUtil.queuePosition.postValue(0)
                ServiceConnector.playFromQueue()
            }
        }
        if (playlist.name == getString(R.string.playlist_recently_added)) {
            adapter = SongsListAdapter(requireContext(), onMoreClicked, onShuffleClickListener)
            binding.btnDelete.visibility = View.GONE
        } else {
            adapter = PlaylistAdapter(requireContext(), onMoreClicked, onShuffleClickListener, object : ItemTouchHelperAdapter {
                override fun onItemMove(fromPosition: Int, toPosition: Int) {
                    PlaylistsUtil.addPlaylist(PlaylistsUtil.removePlaylist(playlist.name).also {
                        Collections.swap(it!!.songs, fromPosition, toPosition)
                    }!!)
                }

                override fun onItemDismiss(position: Int) = Unit
            })
            ItemTouchHelper(object : ItemTouchHelper.Callback() {
                override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int = makeMovementFlags(if (viewHolder.adapterPosition == 0) 0 else DOWN or UP, 0)

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    val toPosition = if (target.adapterPosition == 0) 1 else target.adapterPosition
                    (adapter as ItemTouchHelperAdapter).onItemMove(viewHolder.adapterPosition, toPosition)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

                override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                    if (actionState == ACTION_STATE_DRAG) {
                        viewHolder?.itemView?.setBackgroundColor(Color.BLACK)
                    }
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    viewHolder.itemView.background = null
                }
            }).apply {
                attachToRecyclerView(binding.rvSongs)
                (adapter as PlaylistAdapter).setOnStartDragListener { viewHolder -> startDrag(viewHolder) }
            }

            binding.btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.dialog_delete_playlist_title).setMessage(R.string.dialog_delete_playlist_content).setPositiveButton(R.string.dialog_yes) { _, _ ->
                    PlaylistsUtil.removePlaylist(playlist.name)
                    parentFragmentManager.popBackStack()
                }.setNegativeButton(R.string.dialog_no, null).show()
            }
        }
        binding.tvPlaylistName.text = playlist.name
        binding.rvSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSongs.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.rvSongs.adapter = adapter
        adapter.submitList(playlist.songs)
    }

    companion object {
        private const val TAG = "AmpApp.PlaylistFragment"

        @JvmStatic
        fun newInstance(playlist: Playlist): PlaylistFragment = PlaylistFragment(playlist)
    }
}