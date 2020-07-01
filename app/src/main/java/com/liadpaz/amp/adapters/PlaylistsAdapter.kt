package com.liadpaz.amp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.liadpaz.amp.R
import com.liadpaz.amp.adapters.PlaylistsAdapter.PlaylistViewHolder
import com.liadpaz.amp.databinding.ItemPlaylistBinding
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.viewmodels.Playlist
import java.util.function.Function

class PlaylistsAdapter(private val context: Context, private val onClickListener: OnRecyclerItemClickListener, private val onLongClickListener: Function<Int, Boolean>) : ListAdapter<Playlist, PlaylistViewHolder>(object : DiffUtil.ItemCallback<Playlist>() {
    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem == newItem
    }
}) {
    private val TAG = "AmpApp.PlaylistsAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(ItemPlaylistBinding.inflate(LayoutInflater.from(context), parent, false), onClickListener, onLongClickListener)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.binding.tvPlaylistName.text = playlist!!.name
        if (playlist.songs.size != 0) {
            Glide.with(context).load(playlist.songs[0].coverUri).placeholder(R.drawable.song).into(holder.binding.ivPlaylistCover)
        }
    }

    class PlaylistViewHolder(var binding: ItemPlaylistBinding, onClickListener: OnRecyclerItemClickListener, onLongClickListener: Function<Int, Boolean>) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { v -> onClickListener.onItemClick(v, adapterPosition) }
            binding.root.setOnLongClickListener { onLongClickListener.apply(adapterPosition) }
        }
    }

}