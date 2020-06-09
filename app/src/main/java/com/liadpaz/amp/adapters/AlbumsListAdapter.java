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
import com.liadpaz.amp.databinding.ItemAlbumBinding;
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener;
import com.liadpaz.amp.utils.Utilities;
import com.liadpaz.amp.viewmodels.Album;

public class AlbumsListAdapter extends ListAdapter<Album, AlbumsListAdapter.AlbumViewHolder> {
    private Context context;
    private OnRecyclerItemClickListener onItemClickListener;

    public AlbumsListAdapter(@NonNull Context context, @NonNull OnRecyclerItemClickListener onItemClickListener) {
        super(new DiffUtil.ItemCallback<Album>() {
            @Override
            public boolean areItemsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                return false;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                return false;
            }
        });

        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlbumViewHolder(ItemAlbumBinding.inflate(LayoutInflater.from(context), parent, false), onItemClickListener);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = getItem(position);

        holder.binding.tvAlbumArtist.setText(album.artist);
        holder.binding.tvAlbumName.setText(album.name);

        Glide.with(context).load(Utilities.getCoverUri(album.songs.get(0))).placeholder(R.drawable.song).into(holder.binding.ivAlbumCover);
    }


    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ItemAlbumBinding binding;

        AlbumViewHolder(@NonNull ItemAlbumBinding binding, @NonNull OnRecyclerItemClickListener onItemClickListener) {
            super(binding.getRoot());

            this.binding = binding;
            this.binding.getRoot().setOnClickListener(v -> onItemClickListener.onItemClick(v, getAdapterPosition()));
        }
    }
}
