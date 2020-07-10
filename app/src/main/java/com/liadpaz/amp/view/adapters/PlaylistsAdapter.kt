package com.liadpaz.amp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.databinding.ItemPlaylistBinding
import com.liadpaz.amp.utils.GlideApp
import com.liadpaz.amp.view.adapters.PlaylistsAdapter.PlaylistViewHolder
import com.liadpaz.amp.view.data.Playlist
import com.liadpaz.amp.view.interfaces.OnRecyclerItemClickListener

class PlaylistsAdapter(private val context: Context, private val onClickListener: OnRecyclerItemClickListener, private val onLongClickListener: (Int) -> Boolean) : ListAdapter<Playlist, PlaylistViewHolder>(Playlist.diffCallback) {
    private val TAG = "AmpApp.PlaylistsAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(ItemPlaylistBinding.inflate(LayoutInflater.from(context), parent, false), onClickListener, onLongClickListener)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.binding.tvPlaylistName.text = playlist!!.name
        if (playlist.songs.size != 0) {
            GlideApp.with(context).load(playlist.songs[0].artUri).into(holder.binding.ivPlaylistCover)
        }
    }

    class PlaylistViewHolder(var binding: ItemPlaylistBinding, onClickListener: OnRecyclerItemClickListener, onLongClickListener: (Int) -> Boolean) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { v -> onClickListener(v, adapterPosition) }
            binding.root.setOnLongClickListener { onLongClickListener(adapterPosition) }
        }
    }

}