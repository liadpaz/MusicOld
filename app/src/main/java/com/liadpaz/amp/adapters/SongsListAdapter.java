package com.liadpaz.amp.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.databinding.ItemSongBinding;
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.viewmodels.Song;
import com.liadpaz.amp.utils.Utilities;

import java.util.ArrayList;

public class SongsListAdapter extends ListAdapter<Song, SongsListAdapter.SongViewHolder> {
    @SuppressWarnings("unused")
    private static final String TAG = "SONGS_LIST_ADAPTER";

    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    private Context context;

    public SongsListAdapter(@NonNull Context context, OnRecyclerItemClickListener onRecyclerItemClickListener) {
        super(new DiffUtil.ItemCallback<Song>() {
            @Override
            public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) { return false; }

            @Override
            public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) { return false; }
        });
        this.context = context;
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SongViewHolder(ItemSongBinding.inflate(LayoutInflater.from(context), parent, false), onRecyclerItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = getItem(position);

        holder.binding.tvSongName.setText(song.songTitle);
        holder.binding.tvSongArtist.setText(Utilities.joinArtists(song.songArtists));

        Glide.with(context).load(Utilities.getCoverUri(song)).into(holder.binding.ivSongCover);

        holder.itemView.setOnClickListener(v -> {
            QueueUtil.queue.setValue(new ArrayList<>(getCurrentList()));
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.ACTION_QUEUE_POSITION, position);
            QueueUtil.queuePosition.setValue(position);
            MainActivity.getController().sendCommand(Constants.ACTION_QUEUE_POSITION, bundle, null);
        });
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private ItemSongBinding binding;

        SongViewHolder(@NonNull ItemSongBinding binding, @NonNull OnRecyclerItemClickListener onRecyclerItemClickListener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.btnMore.setOnClickListener(v -> onRecyclerItemClickListener.onItemClick(v, getAdapterPosition()));
        }
    }
}
