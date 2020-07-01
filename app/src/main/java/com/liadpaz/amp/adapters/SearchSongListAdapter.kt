package com.liadpaz.amp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.liadpaz.amp.databinding.ItemNoSongsQueryBinding
import com.liadpaz.amp.databinding.ItemSongBinding
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.viewmodels.Song
import java.util.*

class SearchSongListAdapter(private val context: Context, private val onMoreClickListener: OnRecyclerItemClickListener) : ListAdapter<Song, SearchSongListAdapter.SongViewHolder>(object : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return if (itemCount == 1 && getItem(0) == null) {
            SongViewHolder(ItemNoSongsQueryBinding.inflate(LayoutInflater.from(context), parent, false), onMoreClickListener, OnRecyclerItemClickListener { _, _ -> })
        } else SongViewHolder(ItemSongBinding.inflate(LayoutInflater.from(context), parent, false), onMoreClickListener, OnRecyclerItemClickListener { _, position: Int ->
            QueueUtil.queue.postValue(ArrayList(currentList))
            QueueUtil.queuePosition.postValue(position)
        })
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        if (song != null) {
            val binding = holder.binding as ItemSongBinding
            binding.tvSongName.text = song.title
            binding.tvSongArtist.text = Utilities.joinArtists(song.artists)
            Glide.with(context).load(song.coverUri).into(binding.ivSongCover)
        }
    }

    override fun submitList(list: List<Song>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    class SongViewHolder(val binding: ViewBinding, onMoreClickListener: OnRecyclerItemClickListener, onItemClickListener: OnRecyclerItemClickListener) : RecyclerView.ViewHolder(binding.root) {

        init {
            if (binding is ItemSongBinding) {
                itemView.setOnClickListener { v: View? -> onItemClickListener.onItemClick(v, adapterPosition) }
                binding.btnMore.setOnClickListener { v: View? -> onMoreClickListener.onItemClick(v, adapterPosition) }
            }
        }
    }

    companion object {
        private const val TAG = "AmpApp.SongsListAdapter"
    }

}