package com.liadpaz.amp.view.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.databinding.ItemPlaylistSongBinding
import com.liadpaz.amp.utils.GlideApp
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.view.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.view.interfaces.OnStartDragListener

class PlaylistAdapter(private val onItemClick: (Int) -> Unit, private val onMoreClick: OnRecyclerItemClickListener) : ListAdapter<Song, PlaylistAdapter.SongViewHolder>(Song.diffCallback) {

    private lateinit var onStartDragListener: OnStartDragListener

    fun setOnStartDragListener(onStartDragListener: OnStartDragListener) {
        this.onStartDragListener = onStartDragListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(ItemPlaylistSongBinding.inflate(LayoutInflater.from(parent.context), parent, false), onItemClick, onMoreClick, onStartDragListener)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        val binding = holder.binding
        binding.tvSongName.text = song.title
        binding.tvSongArtist.text = Utilities.joinArtists(song.artists)
        GlideApp.with(holder.itemView).load(song.artUri).into(binding.ivSongCover)
    }

    override fun submitList(list: MutableList<Song>?) {
        Log.d(TAG, "submitList: ")
        super.submitList(list?.let { ArrayList(list) })
    }

    @SuppressLint("ClickableViewAccessibility")
    class SongViewHolder(val binding: ItemPlaylistSongBinding, onItemClick: (Int) -> Unit, onMoreClick: OnRecyclerItemClickListener, startDragListener: OnStartDragListener) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { onItemClick(adapterPosition) }
            binding.btnMore.setOnClickListener { v -> onMoreClick(v, adapterPosition) }
            binding.btnDrag.setOnTouchListener { _, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    startDragListener(this)
                }
                true
            }
        }
    }
}

private const val TAG = "AmpApp.PlaylistAdapter"
