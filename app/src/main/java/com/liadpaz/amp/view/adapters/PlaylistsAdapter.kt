package com.liadpaz.amp.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.databinding.ItemPlaylistBinding
import com.liadpaz.amp.utils.GlideApp
import com.liadpaz.amp.view.adapters.PlaylistsAdapter.PlaylistViewHolder
import com.liadpaz.amp.view.data.Playlist

class PlaylistsAdapter(private val onClickListener: (Int) -> Unit, private val onLongClickListener: (Int) -> Boolean) : ListAdapter<Playlist, PlaylistViewHolder>(Playlist.diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false), onClickListener, onLongClickListener)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.binding.tvPlaylistName.text = playlist!!.name
        if (playlist.songs.isNotEmpty()) {
            GlideApp.with(holder.itemView).load(playlist.songs[0].artUri).into(holder.binding.ivPlaylistCover)
        }
    }

    class PlaylistViewHolder(var binding: ItemPlaylistBinding, onClickListener: (Int) -> Unit, onLongClickListener: (Int) -> Boolean) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { onClickListener(adapterPosition) }
            binding.root.setOnLongClickListener { onLongClickListener(adapterPosition) }
        }
    }
}

private const val TAG = "AmpApp.PlaylistsAdapter"