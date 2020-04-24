package com.liadpaz.music.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.liadpaz.music.databinding.ItemSongBinding;
import com.liadpaz.music.utils.Song;
import com.liadpaz.music.utils.Utilities;

import java.util.TreeSet;

public class SongListAdapter extends BaseAdapter {

    private static final String TAG = "SONGS_LIST_ADAPTER";

    private Activity activity;
    private TreeSet<Song> songs;

    public SongListAdapter(@NonNull Activity activity) {
        super();

        this.activity = activity;
        this.songs = new TreeSet<>((o1, o2) -> o1.getSongName().compareTo(o2.getSongName()));
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.toArray()[position];
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

        binding.getRoot().setClickable(true);

        convertView.setLayoutParams(new ConstraintLayout.LayoutParams(parent.getWidth(), convertView.getHeight()));

        Song song = (Song)getItem(position);

        binding.ivSongCover.setImageBitmap(song.getCover());
        binding.tvSongName.setText(song.getSongName());
        binding.tvSongArtist.setText(Utilities.joinArtists(song.getArtists()));
        binding.btnMore.setOnClickListener(v -> activity.openContextMenu(v));

        return convertView;
    }

    public void addSong(Song song) {
        this.songs.add(song);
        notifyDataSetChanged();
    }
}
