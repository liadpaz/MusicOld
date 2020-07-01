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
import com.liadpaz.amp.adapters.AlbumsListAdapter.AlbumViewHolder
import com.liadpaz.amp.databinding.ItemAlbumBinding
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener
import com.liadpaz.amp.viewmodels.Album

class AlbumsListAdapter(private val context: Context, private val onItemClickListener: OnRecyclerItemClickListener) : ListAdapter<Album, AlbumViewHolder>(object : DiffUtil.ItemCallback<Album>() {
    override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
        return false
        // TODO: fix check
    }

    override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
        return false
        // TODO: fix check
    }
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(ItemAlbumBinding.inflate(LayoutInflater.from(context), parent, false), onItemClickListener)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = getItem(position)
        holder.binding.tvAlbumArtist.text = album!!.artist
        holder.binding.tvAlbumName.text = album.name
        Glide.with(context).load(album.songs[0].coverUri).placeholder(R.drawable.song).into(holder.binding.ivAlbumCover)
    }

    class AlbumViewHolder(var binding: ItemAlbumBinding, onItemClickListener: OnRecyclerItemClickListener) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { v: View? -> onItemClickListener.onItemClick(v, adapterPosition) }
        }
    }

}