package com.liadpaz.amp.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.ItemPlaylistSongBinding
import com.liadpaz.amp.databinding.ItemSongShuffleBinding
import com.liadpaz.amp.interfaces.ItemTouchHelperAdapter
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.interfaces.OnStartDragListener
import com.liadpaz.amp.livedatautils.PlaylistsUtil
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.server.service.ServiceConnector
import com.liadpaz.amp.ui.viewmodels.Song
import com.liadpaz.amp.utils.Utilities
import java.util.*

class PlaylistAdapter(private val context: Context, private val onMoreClick: OnRecyclerItemClickListener, private val onShuffleClick: View.OnClickListener, private val itemTouchHelperAdapter: ItemTouchHelperAdapter) : ListAdapter<Song, PlaylistAdapter.SongViewHolder>(Song.diffCallback), ItemTouchHelperAdapter {

    private lateinit var songs: ArrayList<Song>

    private lateinit var onStartDragListener: OnStartDragListener

    fun setOnStartDragListener(onStartDragListener: OnStartDragListener) {
        this.onStartDragListener = onStartDragListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return if (viewType == TYPE_ITEM) {
            SongViewHolder(ItemPlaylistSongBinding.inflate(LayoutInflater.from(context), parent, false), { _, position: Int ->
                QueueUtil.queue.postValue(ArrayList(songs))
                QueueUtil.queuePosition.postValue(position)
                ServiceConnector.playFromQueue()
            }, onShuffleClick, onMoreClick, onStartDragListener)
        } else SongViewHolder(ItemSongShuffleBinding.inflate(LayoutInflater.from(context), parent, false), { _, _ -> }, onShuffleClick, { _, _ -> }, null)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        if (position != 0) {
            with(getItem(position - 1)) {
                val binding = holder.binding as ItemPlaylistSongBinding
                binding.tvSongName.text = title
                binding.tvSongArtist.text = Utilities.joinArtists(artists)
                Glide.with(context).applyDefaultRequestOptions(glideOptions).load(artUri).into(binding.ivSongCover)
            }
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        PlaylistsUtil.setIsChanging(true)
        Collections.swap(songs, fromPosition - 1, toPosition - 1)
        itemTouchHelperAdapter.onItemMove(fromPosition - 1, toPosition - 1)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) = throw UnsupportedOperationException()

    override fun getItemViewType(position: Int): Int = if (position == 0) 0 else TYPE_ITEM

    override fun getItemCount(): Int = super.getItemCount() + 1

    override fun submitList(list: List<Song>?) = super.submitList(list?.let { ArrayList(it) }.also { songs = it!! })

    @SuppressLint("ClickableViewAccessibility")
    class SongViewHolder(val binding: ViewBinding, onItemClick: OnRecyclerItemClickListener, onShuffleClick: View.OnClickListener, onMoreClick: OnRecyclerItemClickListener, startDragListener: OnStartDragListener?) : RecyclerView.ViewHolder(binding.root) {

        init {
            if (binding is ItemSongShuffleBinding) {
                binding.root.setOnClickListener(onShuffleClick)
            } else {
                itemView.setOnClickListener { v: View -> onItemClick(v, adapterPosition - 1) }
                binding as ItemPlaylistSongBinding
                binding.btnMore.setOnClickListener { v -> onMoreClick(v, adapterPosition - 1) }
                binding.btnDrag.setOnTouchListener { _, event: MotionEvent ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        startDragListener!!(this)
                    }
                    true
                }
            }
        }
    }

    companion object {
        private const val TAG = "AmpApp.PlaylistAdapter"
        private const val TYPE_ITEM = 1
    }
}

private val glideOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .placeholder(R.drawable.song)
        .fallback(R.drawable.song)
        .error(R.drawable.song)