package com.liadpaz.music.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;

import com.liadpaz.music.R;
import com.liadpaz.music.databinding.ItemSongBinding;
import com.liadpaz.music.utils.Song;
import com.liadpaz.music.utils.Utilities;

import java.util.List;
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

        Song song = (Song)getItem(position);

        byte[] data = song.getCover();
        if (data != null) {
            new FutureTask<>(() -> {
                Bitmap cover = BitmapFactory.decodeByteArray(data, 0, data.length);
                binding.ivSongCover.setImageBitmap(cover);
            }, null).run();
        } else {
            binding.ivSongCover.setImageResource(R.drawable.ic_audiotrack_black_24dp);
        }
        binding.tvSongName.setText(song.getSongName());
        binding.tvSongArtist.setText(Utilities.joinArtists(song.getArtists()));
        activity.registerForContextMenu(binding.btnMore);
        binding.btnMore.setOnClickListener(v -> {
            Log.d(TAG, "getView: context menu open");
            activity.openContextMenu(v);
        });

        return convertView;
    }

    public void setSource(List<Song> sourceSongs) {
        this.songs.addAll(sourceSongs);
        notifyDataSetChanged();
    }

    public Song[] getSongs() {
        return songs.toArray(new Song[]{});
    }
}
