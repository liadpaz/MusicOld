package com.liadpaz.amp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.databinding.ItemNoSongsBinding;
import com.liadpaz.amp.databinding.ItemSongBinding;
import com.liadpaz.amp.databinding.ItemSongShuffleBinding;
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.Utilities;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;

public class SongsListAdapter extends ListAdapter<Song, SongsListAdapter.SongViewHolder> {
    private static final String TAG = "AmpApp.SongsListAdapter";

    private static final int TYPE_ITEM = 1;

    private OnRecyclerItemClickListener onMoreClickListener;
    private View.OnClickListener onShuffleClickListener;

    private Context context;

    public SongsListAdapter(@NonNull Context context, @NonNull OnRecyclerItemClickListener onMoreClickListener, @NonNull View.OnClickListener onShuffleClickListener) {
        super(new DiffUtil.ItemCallback<Song>() {
            @Override
            public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) { return oldItem == newItem; }

            @Override
            public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) { return oldItem.equals(newItem); }
        });
        this.context = context;
        this.onMoreClickListener = onMoreClickListener;
        this.onShuffleClickListener = onShuffleClickListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (getItemCount() == 1) {
            return new SongViewHolder(ItemNoSongsBinding.inflate(LayoutInflater.from(context), parent, false), onShuffleClickListener);
        } else if (viewType == TYPE_ITEM) {
            return new SongViewHolder(ItemSongBinding.inflate(LayoutInflater.from(context), parent, false), onShuffleClickListener);
        }
        return new SongViewHolder(ItemSongShuffleBinding.inflate(LayoutInflater.from(context), parent, false), onShuffleClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        if (position != 0) {
            Song song = getItem(position - 1);

            ItemSongBinding binding = (ItemSongBinding)holder.binding;

            binding.tvSongName.setText(song.songTitle);
            binding.tvSongArtist.setText(Utilities.joinArtists(song.songArtists));

            Glide.with(context).load(Utilities.getCoverUri(song)).into(binding.ivSongCover);

            binding.btnMore.setOnClickListener(v -> onMoreClickListener.onItemClick(v, position - 1));
            binding.getRoot().setOnClickListener(v -> {
                QueueUtil.queue.setValue(new ArrayList<>(getCurrentList()));
                QueueUtil.setPosition(position - 1);
                MainActivity.getController().sendCommand(Constants.ACTION_QUEUE_POSITION, null, null);
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private ViewBinding binding;

        SongViewHolder(@NonNull ViewBinding binding, @NonNull View.OnClickListener onClickListener) {
            super(binding.getRoot());
            this.binding = binding;
            if (binding instanceof ItemSongShuffleBinding) {
                ((ItemSongShuffleBinding)binding).getRoot().setOnClickListener(onClickListener);
            }
        }
    }
}
