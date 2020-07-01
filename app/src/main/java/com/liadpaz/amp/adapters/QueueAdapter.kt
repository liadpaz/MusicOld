package com.liadpaz.amp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.ItemQueueSongBinding
import com.liadpaz.amp.interfaces.ItemTouchHelperAdapter
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.interfaces.OnStartDragListener
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.service.ServiceConnector.Companion.getInstance
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.viewmodels.Song
import java.util.*

class QueueAdapter(fragment: Fragment, private val onMoreClickListener: OnRecyclerItemClickListener, private val itemTouchHelperAdapter: ItemTouchHelperAdapter) : ListAdapter<Song, QueueAdapter.SongViewHolder>(object : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }
}), ItemTouchHelperAdapter {
    private val TAG = "AmpApp.QueueAdapter"

    private lateinit var onStartDragListener: OnStartDragListener
    private lateinit var mediaSource: ConcatenatingMediaSource

    private var songs: ArrayList<Song> = ArrayList()

    private val context: Context = fragment.requireContext()

    fun setOnStartDragListener(onStartDragListener: OnStartDragListener) {
        this.onStartDragListener = onStartDragListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder = SongViewHolder(ItemQueueSongBinding.inflate(LayoutInflater.from(context), parent, false), OnRecyclerItemClickListener { _, position ->
        getInstance()!!.transportControls!!.skipToQueueItem(position.toLong())
    }, OnRecyclerItemClickListener { v, position ->
        onMoreClickListener.onItemClick(v, position)
    })

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        val binding = holder.binding
        binding.tvSongTitle.text = song!!.title
        binding.tvSongArtist.text = Utilities.joinArtists(song.artists)
        Glide.with(context).load(song.coverUri).placeholder(R.drawable.song).into(binding.ivSongCover)
        binding.btnDrag.setOnTouchListener { _, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                onStartDragListener.onStartDrag(holder)
            }
            true
        }
    }

    override fun onItemDismiss(position: Int) {
        songs.removeAt(position)
        QueueUtil.isChanging = true
        QueueUtil.queue.postValue(songs)
        mediaSource.removeMediaSource(position)
        itemTouchHelperAdapter.onItemDismiss(position)
        notifyItemRemoved(position)
        Toast.makeText(context, context.getString(R.string.queue_removed, getItem(position).title), Toast.LENGTH_SHORT).show()
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(songs, fromPosition, toPosition)
        QueueUtil.isChanging = true
        QueueUtil.queue.postValue(songs)
        mediaSource.moveMediaSource(fromPosition, toPosition)
        itemTouchHelperAdapter.onItemMove(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun submitList(list: List<Song>?) {
        super.submitList(list?.let { ArrayList(it) }.also { songs = it!! })
    }

    class SongViewHolder(val binding: ItemQueueSongBinding, onItemClickListener: OnRecyclerItemClickListener, onMoreClickListener: OnRecyclerItemClickListener) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { v: View? -> onItemClickListener.onItemClick(v, adapterPosition) }
            binding.btnMore.setOnClickListener { v: View? -> onMoreClickListener.onItemClick(v, adapterPosition) }
        }
    }

    init {
        getInstance()!!.mediaSource.observeForever { mediaSource: ConcatenatingMediaSource? -> this.mediaSource = mediaSource!! }
    }
}