package com.liadpaz.amp.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.liadpaz.amp.databinding.ItemNoSongsBinding
import com.liadpaz.amp.databinding.ItemSongBinding
import com.liadpaz.amp.databinding.ItemSongShuffleBinding
import com.liadpaz.amp.utils.GlideApp
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.view.interfaces.OnRecyclerItemClickListener

class SongsListAdapter(private val onItemClick: (Int) -> Unit, private val onMoreClickListener: OnRecyclerItemClickListener, private val onShuffleClickListener: () -> Unit) : ListAdapter<Song, SongsListAdapter.SongViewHolder>(Song.diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder = when (viewType) {
        // show the normal song item
        TYPE_ITEM -> SongViewHolder(ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false), onItemClick, onShuffleClickListener, onMoreClickListener)
        // show the shuffle item
        TYPE_SHUFFLE -> SongViewHolder(ItemSongShuffleBinding.inflate(LayoutInflater.from(parent.context), parent, false), { }, onShuffleClickListener, { _, _ -> })
        // show the 'no items' item
        else -> SongViewHolder(ItemNoSongsBinding.inflate(LayoutInflater.from(parent.context), parent, false), { }, { }, { _, _ -> })
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        if (itemCount != 0 && position != 0) {
            val song = getItem(position - 1)
            val binding = holder.binding as ItemSongBinding
            binding.tvSongName.text = song!!.title
            binding.tvSongArtist.text = Utilities.joinArtists(song.artists)
            GlideApp.with(holder.itemView).load(song.artUri).into(binding.ivSongCover)
        }
    }

    override fun getItemViewType(position: Int): Int = if (itemCount == 1) TYPE_NO_ITEMS else if (position == 0) TYPE_SHUFFLE else TYPE_ITEM

    override fun getItemCount(): Int = super.getItemCount() + 1

    override fun submitList(list: List<Song>?) {
        super.submitList(list?.let { ArrayList(list) })
    }

    class SongViewHolder(val binding: ViewBinding, onItemClick: (Int) -> Unit, onShuffleClick: () -> Unit, onMoreClick: OnRecyclerItemClickListener) : RecyclerView.ViewHolder(binding.root) {

        init {
            if (binding is ItemSongShuffleBinding) {
                binding.root.setOnClickListener { onShuffleClick() }
            } else if (binding is ItemSongBinding) {
                itemView.setOnClickListener { onItemClick(adapterPosition - 1) }
                binding.btnMore.setOnClickListener { v: View -> onMoreClick(v, adapterPosition - 1) }
            }
        }
    }

    companion object {
        private const val TYPE_SHUFFLE = 0
        private const val TYPE_ITEM = 1
        private const val TYPE_NO_ITEMS = 2
    }
}

private const val TAG = "AmpApp.SongsListAdapter"