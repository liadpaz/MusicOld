package com.liadpaz.amp.fragments

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ItemTouchHelper.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liadpaz.amp.R
import com.liadpaz.amp.adapters.PlaylistAdapter
import com.liadpaz.amp.adapters.SongsListAdapter
import com.liadpaz.amp.databinding.FragmentPlaylistBinding
import com.liadpaz.amp.dialogs.PlaylistsDialog
import com.liadpaz.amp.interfaces.ItemTouchHelperAdapter
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.interfaces.OnStartDragListener
import com.liadpaz.amp.livedatautils.PlaylistsUtil
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.viewmodels.Playlist
import com.liadpaz.amp.viewmodels.Song
import java.util.*

class PlaylistFragment private constructor(private val playlist: Playlist?) : Fragment() {
    private val TAG = "AmpApp.PlaylistFragment"

    private lateinit var adapter: ListAdapter<Song, out RecyclerView.ViewHolder>

    private lateinit var binding: FragmentPlaylistBinding

    override fun onCreateView(inflater: LayoutInflater, container:
    ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentPlaylistBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val onMoreClicked = OnRecyclerItemClickListener { v: View?, position: Int ->
            PopupMenu(requireContext(), v).apply {
                inflate(if (playlist!!.name == getString(R.string.playlist_recently_added)) R.menu.menu_playlist_recently else R.menu.menu_playlist)
                setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menuPlayNext -> {
//                                AmpPlaybackPreparer.addToQueueNext(adapter.currentList[position])
                            // TODO: play next
                        }
                        R.id.menuAddQueue -> {
//                                AmpPlaybackPreparer.addToQueue(adapter.currentList[position])
                            // TODO: add to queue
                        }
                        R.id.menuQueueAddPlaylist -> {
                            PlaylistsDialog(adapter.currentList[position]).show(childFragmentManager, null)
                        }
                        R.id.menuRemoveFromPlaylist -> {
                            val playlist = PlaylistsUtil.removePlaylist(playlist.name)
                            playlist!!.songs.removeAt(position)
                            adapter.notifyItemRemoved(position)
                            PlaylistsUtil.addPlaylist(playlist)
                        }
                    }
                    true
                }
                show()
            }
        }
        val onShuffleClickListener = View.OnClickListener {
            if (playlist!!.songs.size != 0) {
                val queue = ArrayList(playlist.songs)
                queue.shuffle()
                QueueUtil.queue.postValue(queue)
                QueueUtil.queuePosition.postValue(0)
                // TODO: play queue
            }
        }
        if (playlist!!.name == getString(R.string.playlist_recently_added)) {
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
                override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                    return makeMovementFlags(if (viewHolder.adapterPosition == 0) 0 else DOWN or UP, 0)
                }

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    val toPosition = if (target.adapterPosition == 0) 1 else target.adapterPosition
                    (adapter as ItemTouchHelperAdapter).onItemMove(viewHolder.adapterPosition, toPosition)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
                override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                    if (actionState == ACTION_STATE_DRAG) {
                        viewHolder!!.itemView.setBackgroundColor(Color.BLACK)
                    }
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    viewHolder.itemView.background = null
                }
            }).apply {
                attachToRecyclerView(binding.rvSongs)
                (adapter as PlaylistAdapter).setOnStartDragListener(OnStartDragListener{ viewHolder: RecyclerView.ViewHolder -> startDrag(viewHolder) })
            }
            binding.btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.dialog_delete_playlist_title).setMessage(R.string.dialog_delete_playlist_content).setPositiveButton(R.string.dialog_yes) { _, _ ->
                    PlaylistsUtil.removePlaylist(playlist.name)
                    parentFragmentManager.popBackStack()
                }.setNegativeButton(R.string.dialog_no, null).show()
            }
            PlaylistsUtil.observe(requireActivity(), Observer { playlists: Queue<Playlist> ->
                if (!PlaylistsUtil.getIsChanging()) {
                    for (playlist in playlists) {
                        if (playlist.name == this.playlist.name) {
                            adapter.submitList(playlist.songs)
                            return@Observer
                        }
                    }
                } else {
                    PlaylistsUtil.setIsChanging(false)
                }
            })
        }
        binding.tvPlaylistName.text = playlist.name
        binding.rvSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSongs.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.rvSongs.adapter = adapter
        adapter.submitList(playlist.songs)
    }

    companion object {
        fun newInstance(playlist: Playlist?): PlaylistFragment {
            return PlaylistFragment(playlist)
        }
    }
}