package com.liadpaz.music.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.liadpaz.music.R;
import com.liadpaz.music.databinding.ItemSongBinding;
import com.liadpaz.music.utils.Song;
import com.liadpaz.music.utils.Utilities;

import java.util.TreeSet;
import java.util.concurrent.FutureTask;

public class SongListAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
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

        new FutureTask<>(() -> {
            byte[] data = song.getCover();
            if (data != null) {
                Bitmap cover = BitmapFactory.decodeByteArray(data, 0, data.length);
                binding.ivSongCover.setImageBitmap(cover);
            } else {
                binding.ivSongCover.setImageResource(R.drawable.ic_audiotrack_black_24dp);
            }
        }, null).run();
        binding.tvSongName.setText(song.getSongName());
        binding.tvSongArtist.setText(Utilities.joinArtists(song.getArtists()));
        binding.btnMore.setOnClickListener(v -> activity.openContextMenu(v));

        return convertView;
    }

    public void addSong(final Song song) {
        this.songs.add(song);
        notifyDataSetInvalidated();
    }
}
