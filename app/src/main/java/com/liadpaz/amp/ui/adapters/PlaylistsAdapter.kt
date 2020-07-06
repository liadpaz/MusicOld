package com.liadpaz.amp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.liadpaz.amp.R
import com.liadpaz.amp.ui.adapters.PlaylistsAdapter.PlaylistViewHolder
import com.liadpaz.amp.databinding.ItemPlaylistBinding
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.ui.viewmodels.Playlist
import java.util.function.Function

class PlaylistsAdapter(private val context: Context, private val onClickListener: OnRecyclerItemClickListener, private val onLongClickListener: (Int) -> Boolean) : ListAdapter<Playlist, PlaylistViewHolder>(Playlist.diffCallback) {
    private val TAG = "AmpApp.PlaylistsAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(ItemPlaylistBinding.inflate(LayoutInflater.from(context), parent, false), onClickListener, onLongClickListener)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.binding.tvPlaylistName.text = playlist!!.name
        if (playlist.songs.size != 0) {
            Glide.with(context).load(playlist.songs[0].artUri).placeholder(R.drawable.song).into(holder.binding.ivPlaylistCover)
        }
    }

    class PlaylistViewHolder(var binding: ItemPlaylistBinding, onClickListener: OnRecyclerItemClickListener, onLongClickListener: (Int) -> Boolean) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { v -> onClickListener(v, adapterPosition) }
            binding.root.setOnLongClickListener { onLongClickListener(adapterPosition) }
        }
    }

}