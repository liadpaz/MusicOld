package com.liadpaz.amp.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.liadpaz.amp.databinding.ItemNoSongsQueryBinding
import com.liadpaz.amp.databinding.ItemSongBinding
import com.liadpaz.amp.utils.GlideApp
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.view.interfaces.OnRecyclerItemClickListener
import java.util.*

class SearchSongListAdapter(private val onItemClick: (Int) -> Unit, private val onMoreClick: OnRecyclerItemClickListener) : ListAdapter<Song, SearchSongListAdapter.SongViewHolder>(Song.diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return if (itemCount == 1 && getItem(0) == null) {
            SongViewHolder(ItemNoSongsQueryBinding.inflate(LayoutInflater.from(parent.context), parent, false), onMoreClick, { })
        } else SongViewHolder(ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false), onMoreClick, onItemClick)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        if (song != null) {
            val binding = holder.binding as ItemSongBinding
            binding.tvSongName.text = song.title
            binding.tvSongArtist.text = Utilities.joinArtists(song.artists)
            GlideApp.with(holder.itemView).load(song.artUri).into(binding.ivSongCover)
        }
    }

    override fun submitList(list: List<Song>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    class SongViewHolder(val binding: ViewBinding, onMoreClickListener: OnRecyclerItemClickListener, onItemClickListener: (Int) -> Unit) : RecyclerView.ViewHolder(binding.root) {

        init {
            if (binding is ItemSongBinding) {
                itemView.setOnClickListener { onItemClickListener(adapterPosition) }
                binding.btnMore.setOnClickListener { v: View -> onMoreClickListener(v, adapterPosition) }
            }
        }
    }

    companion object {
        private const val TAG = "AmpApp.SongsListAdapter"
    }

}