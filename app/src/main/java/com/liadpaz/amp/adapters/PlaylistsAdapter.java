package com.liadpaz.amp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.ItemPlaylistBinding;
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener;
import com.liadpaz.amp.utils.Utilities;
import com.liadpaz.amp.viewmodels.Playlist;

import java.util.function.Consumer;

public class PlaylistsAdapter extends ListAdapter<Playlist, PlaylistsAdapter.PlaylistViewHolder> {
    private Context context;
    private OnRecyclerItemClickListener onClickListener;
    private Consumer<Integer> onLongClickListener;

    public PlaylistsAdapter(@NonNull Context context, @NonNull OnRecyclerItemClickListener onClickListener, @NonNull Consumer<Integer> onLongClickListener) {
        super(new DiffUtil.ItemCallback<Playlist>() {
            @Override
            public boolean areItemsTheSame(@NonNull Playlist oldItem, @NonNull Playlist newItem) {
                return false;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Playlist oldItem, @NonNull Playlist newItem) {
                return false;
            }
        });

        this.context = context;
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaylistViewHolder(ItemPlaylistBinding.inflate(LayoutInflater.from(context), parent, false), onClickListener, onLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = getItem(position);

        holder.binding.tvPlaylistName.setText(playlist.name);
        if (playlist.songs.size() != 0) {
            Glide.with(context).load(Utilities.getCoverUri(playlist.songs.get(0))).placeholder(R.drawable.song).into(holder.binding.ivPlaylistCover);
        }
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ItemPlaylistBinding binding;

        PlaylistViewHolder(@NonNull ItemPlaylistBinding binding, @NonNull OnRecyclerItemClickListener onClickListener, @NonNull Consumer<Integer> onLongClickListener) {
            super(binding.getRoot());

            this.binding = binding;
            this.binding.getRoot().setOnClickListener(v -> onClickListener.onItemClick(v, getAdapterPosition()));
            this.binding.getRoot().setOnLongClickListener(v -> {
                onLongClickListener.accept(getAdapterPosition());
                return true;
            });
        }
    }
}
