package com.liadpaz.amp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.ItemArtistBinding
import com.liadpaz.amp.viewmodels.Artist

class ArtistsListAdapter(context: Context, artists: List<Artist?>) : ArrayAdapter<Artist?>(context, R.layout.item_artist, artists) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var root = convertView
        val binding: ItemArtistBinding
        if (root == null) {
            binding = ItemArtistBinding.inflate(LayoutInflater.from(context), parent, false)
            root = binding.root
            root.setTag(binding)
        } else {
            binding = root.tag as ItemArtistBinding
        }
        val artist = getItem(position)!!
        binding.tvArtistName.text = artist.name
        binding.tvArtistCount.text = artist.songs.size.toString()
        return root
    }
}