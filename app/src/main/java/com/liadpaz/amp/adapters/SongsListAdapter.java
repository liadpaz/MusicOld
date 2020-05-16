package com.liadpaz.amp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.ItemSongBinding;
import com.liadpaz.amp.utils.Song;
import com.liadpaz.amp.utils.Utilities;

import java.util.ArrayList;

public class SongsListAdapter extends ArrayAdapter<Song> {

    @SuppressWarnings("unused")
    private static final String TAG = "SONGS_LIST_ADAPTER";

    public SongsListAdapter(@NonNull Context context, ArrayList<Song> songs) {
        super(context, R.layout.item_song, songs);
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SongViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new SongViewHolder(ItemSongBinding.inflate(LayoutInflater.from(getContext()), parent, false));
            convertView = viewHolder.itemView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SongViewHolder)convertView.getTag();
        }

        Song song = getItem(position);

        viewHolder.binding.tvSongName.setText(song.getSongTitle());
        viewHolder.binding.tvSongArtist.setText(Utilities.joinArtists(song.getSongArtists()));
        Uri cover = Utilities.getCover(song);
        if (cover != null) {
            viewHolder.binding.ivSongCover.setImageURI(cover);
        } else {
            viewHolder.binding.ivSongCover.setImageResource(R.drawable.song);
        }

        return convertView;
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private final ItemSongBinding binding;

        SongViewHolder(@NonNull ItemSongBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
