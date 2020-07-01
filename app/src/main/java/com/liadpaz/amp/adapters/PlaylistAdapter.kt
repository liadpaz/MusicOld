package com.liadpaz.amp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.liadpaz.amp.databinding.ItemPlaylistSongBinding
import com.liadpaz.amp.databinding.ItemSongShuffleBinding
import com.liadpaz.amp.interfaces.ItemTouchHelperAdapter
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.interfaces.OnStartDragListener
import com.liadpaz.amp.livedatautils.PlaylistsUtil
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.viewmodels.Song
import java.util.*

class PlaylistAdapter(private val context: Context, private val onMoreClickListener: OnRecyclerItemClickListener, private val onShuffleClickListener: View.OnClickListener, private val itemTouchHelperAdapter: ItemTouchHelperAdapter) : ListAdapter<Song, PlaylistAdapter.SongViewHolder>(object : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }
}), ItemTouchHelperAdapter {
    private lateinit var songs: ArrayList<Song>
    private var onStartDragListener: OnStartDragListener? = null
    fun setOnStartDragListener(onStartDragListener: OnStartDragListener) {
        this.onStartDragListener = onStartDragListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return if (viewType == TYPE_ITEM) {
            SongViewHolder(ItemPlaylistSongBinding.inflate(LayoutInflater.from(context), parent, false), OnRecyclerItemClickListener { _, position: Int ->
                QueueUtil.queue.postValue(ArrayList(songs))
                QueueUtil.queuePosition.postValue(position - 1)
                // TODO: play from queue
            }, onShuffleClickListener)
        } else SongViewHolder(ItemSongShuffleBinding.inflate(LayoutInflater.from(context), parent, false), OnRecyclerItemClickListener { _, _ -> }, onShuffleClickListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        if (position != 0) {
            val song = getItem(position - 1)
            val binding = holder.binding as ItemPlaylistSongBinding
            binding.tvSongName.text = song!!.title
            binding.tvSongArtist.text = Utilities.joinArtists(song.artists)
            Glide.with(context).load(song.coverUri).into(binding.ivSongCover)
            binding.btnMore.setOnClickListener { v: View? -> onMoreClickListener.onItemClick(v, position - 1) }
            binding.btnDrag.setOnTouchListener { _, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    onStartDragListener!!.onStartDrag(holder)
                }
                true
            }
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        PlaylistsUtil.setIsChanging(true)
        Collections.swap(songs, fromPosition - 1, toPosition - 1)
        itemTouchHelperAdapter.onItemMove(fromPosition - 1, toPosition - 1)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
        throw UnsupportedOperationException()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun submitList(list: List<Song>?) {
        super.submitList(list?.let { ArrayList(it) }.also { songs = it!! })
    }

    class SongViewHolder(val binding: ViewBinding, onItemClick: OnRecyclerItemClickListener, onShuffleClickListener: View.OnClickListener) : RecyclerView.ViewHolder(binding.root) {

        init {
            if (binding is ItemSongShuffleBinding) {
                binding.root.setOnClickListener(onShuffleClickListener)
            } else {
                itemView.setOnClickListener { v: View? -> onItemClick.onItemClick(v, adapterPosition) }
            }
        }
    }

    companion object {
        private const val TAG = "AmpApp.PlaylistAdapter"
        private const val TYPE_ITEM = 1
    }

}