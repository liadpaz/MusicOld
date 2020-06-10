package com.liadpaz.amp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.liadpaz.amp.databinding.ItemNoSongsQueryBinding;
import com.liadpaz.amp.databinding.ItemSongBinding;
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.utils.Utilities;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.List;

public class SearchSongListAdapter extends ListAdapter<Song, SearchSongListAdapter.SongViewHolder> {
    private static final String TAG = "AmpApp.SongsListAdapter";

    private OnRecyclerItemClickListener onMoreClickListener;

    private Context context;

    public SearchSongListAdapter(@NonNull Context context, @NonNull OnRecyclerItemClickListener onMoreClickListener) {
        super(new DiffUtil.ItemCallback<Song>() {
            @Override
            public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) { return oldItem == newItem; }

            @Override
            public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) { return oldItem.equals(newItem); }
        });
        this.context = context;
        this.onMoreClickListener = onMoreClickListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (getItemCount() == 1 && getItem(0) == null) {
            return new SongViewHolder(ItemNoSongsQueryBinding.inflate(LayoutInflater.from(context), parent, false), onMoreClickListener, (v, position) -> {});
        }
        return new SongViewHolder(ItemSongBinding.inflate(LayoutInflater.from(context), parent, false), onMoreClickListener, (v, position) -> {
            QueueUtil.setQueue(new ArrayList<>(getCurrentList()));
            QueueUtil.setPosition(position);
        });

    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = getItem(position);

        if (song != null) {
            ItemSongBinding binding = (ItemSongBinding)holder.binding;

            binding.tvSongName.setText(song.songTitle);
            binding.tvSongArtist.setText(Utilities.joinArtists(song.songArtists));

            Glide.with(context).load(Utilities.getCoverUri(song)).into(binding.ivSongCover);
        }
    }

    @Override
    public void submitList(@Nullable List<Song> list) {
        super.submitList(list != null ? new ArrayList<>(list) : null);
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private ViewBinding binding;

        SongViewHolder(@NonNull ViewBinding binding, @NonNull OnRecyclerItemClickListener onMoreClickListener, @NonNull OnRecyclerItemClickListener onItemClickListener) {
            super(binding.getRoot());
            this.binding = binding;
            if (binding instanceof ItemSongBinding) {
                itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, getAdapterPosition()));
                ((ItemSongBinding)binding).btnMore.setOnClickListener(v -> onMoreClickListener.onItemClick(v, getAdapterPosition()));
            }
        }
    }
}
