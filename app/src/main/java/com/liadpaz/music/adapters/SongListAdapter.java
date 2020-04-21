package com.liadpaz.music.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.liadpaz.music.databinding.ItemSongBinding;
import com.liadpaz.music.utils.Song;

import java.util.ArrayList;

public class SongListAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<Song> songs;

    public SongListAdapter(@NonNull Activity activity, @NonNull ArrayList<Song> songs) {
        super();

        this.activity = activity;
        this.songs = songs;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemSongBinding binding;
        if (convertView == null) {
            binding = ItemSongBinding.inflate(activity.getLayoutInflater());
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (ItemSongBinding)convertView.getTag();
        }

        convertView.setLayoutParams(new ConstraintLayout.LayoutParams(parent.getWidth(), convertView.getHeight()));

        Song song = songs.get(position);
// TODO: add cover image
        binding.ivSongCover.setImageDrawable(song.getCover());
        binding.tvSongName.setText(song.getSongName());
        binding.tvSongArtist.setText(song.getArtists().get(0));

        return convertView;
    }
}
