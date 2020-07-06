package com.liadpaz.amp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.liadpaz.amp.databinding.ItemNoSongsBinding
import com.liadpaz.amp.databinding.ItemSongBinding
import com.liadpaz.amp.databinding.ItemSongShuffleBinding
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.server.service.ServiceConnector
import com.liadpaz.amp.ui.viewmodels.Song
import com.liadpaz.amp.utils.Utilities
import java.util.*

class SongsListAdapter(private val context: Context, private val onMoreClickListener: OnRecyclerItemClickListener, private val onShuffleClickListener: View.OnClickListener) : ListAdapter<Song, SongsListAdapter.SongViewHolder>(Song.diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        if (itemCount == 1) {
            // no songs, so show the no songs item
            return SongViewHolder(ItemNoSongsBinding.inflate(LayoutInflater.from(context), parent, false), { _, _ -> }, onShuffleClickListener, { _, _ -> })
        } else if (viewType == TYPE_ITEM) {
            // show the normal song item
            return SongViewHolder(ItemSongBinding.inflate(LayoutInflater.from(context), parent, false), { _, position: Int ->
                QueueUtil.queue.postValue(ArrayList(currentList))
                QueueUtil.queuePosition.postValue(position)
                ServiceConnector.playFromQueue()
            }, onShuffleClickListener, onMoreClickListener)
        }
        // show the shuffle item
        return SongViewHolder(ItemSongShuffleBinding.inflate(LayoutInflater.from(context), parent, false), { _, _ -> }, onShuffleClickListener, { _, _ -> })
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        if (position != 0) {
            val song = getItem(position - 1)
            val binding = holder.binding as ItemSongBinding
            binding.tvSongName.text = song!!.title
            binding.tvSongArtist.text = Utilities.joinArtists(song.artists)
            Glide.with(context).load(song.artUri).into(binding.ivSongCover)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun submitList(list: List<Song>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    class SongViewHolder(val binding: ViewBinding, onItemClick: OnRecyclerItemClickListener, onShuffleClick: View.OnClickListener, onMoreClick: OnRecyclerItemClickListener) : RecyclerView.ViewHolder(binding.root) {

        init {
            if (binding is ItemSongShuffleBinding) {
                binding.root.setOnClickListener(onShuffleClick)
            } else if (binding is ItemSongBinding) {
                itemView.setOnClickListener { v: View -> onItemClick(v, adapterPosition - 1) }
                binding.btnMore.setOnClickListener { v: View -> onMoreClick(v, adapterPosition - 1) }
            }
        }
    }

    companion object {
        private const val TAG = "AmpApp.SongsListAdapter"
        private const val TYPE_ITEM = 1
    }

}