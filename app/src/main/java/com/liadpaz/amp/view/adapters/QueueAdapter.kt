package com.liadpaz.amp.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.liadpaz.amp.databinding.ItemQueueSongBinding
import com.liadpaz.amp.utils.GlideApp
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.view.interfaces.ItemTouchHelperAdapter
import com.liadpaz.amp.view.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.view.interfaces.OnStartDragListener

class QueueAdapter(private val onItemClick: (Int) -> Unit, private val onMoreClickListener: OnRecyclerItemClickListener, private val itemTouchHelperAdapter: ItemTouchHelperAdapter) : ListAdapter<Song, QueueAdapter.SongViewHolder>(Song.diffCallback) {

    private lateinit var onStartDragListener: OnStartDragListener
    private lateinit var mediaSource: ConcatenatingMediaSource

    private var songs: ArrayList<Song> = ArrayList()

    fun setOnStartDragListener(onStartDragListener: OnStartDragListener) {
        this.onStartDragListener = onStartDragListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(ItemQueueSongBinding.inflate(LayoutInflater.from(parent.context), parent, false), onItemClick, onMoreClickListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        val binding = holder.binding
        binding.tvSongTitle.text = song!!.title
        binding.tvSongArtist.text = Utilities.joinArtists(song.artists)
        GlideApp.with(holder.itemView).load(song.artUri).into(binding.ivSongCover)
        binding.btnDrag.setOnTouchListener { _, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                onStartDragListener(holder)
            }
            true
        }
    }

//    override fun onItemDismiss(position: Int) {
//        Toast.makeText(context, context.getString(R.string.queue_removed, getItem(position).title), Toast.LENGTH_SHORT).show()
//        songs.removeAt(position)
//        QueueUtil.isChanging = true
//        QueueUtil.queue.postValue(songs)
//        mediaSource.removeMediaSource(position)
//        itemTouchHelperAdapter.onItemDismiss(position)
//        notifyItemRemoved(position)
//    }

//    override fun onItemMove(fromPosition: Int, toPosition: Int) {
//        Collections.swap(songs, fromPosition, toPosition)
//        QueueUtil.isChanging = true
//        QueueUtil.queue.postValue(songs)
//        mediaSource.moveMediaSource(fromPosition, toPosition)
//        itemTouchHelperAdapter.onItemMove(fromPosition, toPosition)
//        notifyItemMoved(fromPosition, toPosition)
//    }

    override fun submitList(list: List<Song>?) {
        super.submitList(list?.let { ArrayList(it) }.also { songs = it!! })
    }

    class SongViewHolder(val binding: ItemQueueSongBinding, onItemClickListener: (Int) -> Unit, onMoreClickListener: OnRecyclerItemClickListener) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { onItemClickListener(adapterPosition) }
            binding.btnMore.setOnClickListener { v: View -> onMoreClickListener(v, adapterPosition) }
        }
    }
}

private const val TAG = "AmpApp.QueueAdapter"