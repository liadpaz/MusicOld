package com.liadpaz.amp.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.databinding.ItemAlbumBinding
import com.liadpaz.amp.utils.GlideApp
import com.liadpaz.amp.view.adapters.AlbumsListAdapter.AlbumViewHolder
import com.liadpaz.amp.view.data.Album
import com.liadpaz.amp.view.interfaces.OnRecyclerItemClickListener

class AlbumsListAdapter(private val onItemClickListener: OnRecyclerItemClickListener) : ListAdapter<Album, AlbumViewHolder>(Album.diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false), onItemClickListener)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = getItem(position)
        val binding = holder.binding
        binding.tvAlbumArtist.text = album!!.artist
        binding.tvAlbumName.text = album.name
        GlideApp.with(holder.itemView).load(album.songs[0].artUri).into(holder.binding.ivAlbumCover)
    }

    class AlbumViewHolder(var binding: ItemAlbumBinding, onItemClickListener: OnRecyclerItemClickListener) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { v: View -> onItemClickListener(v, adapterPosition) }
        }
    }

}