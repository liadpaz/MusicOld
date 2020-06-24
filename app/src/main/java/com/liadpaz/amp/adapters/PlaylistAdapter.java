package com.liadpaz.amp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.liadpaz.amp.databinding.ItemPlaylistSongBinding;
import com.liadpaz.amp.databinding.ItemSongShuffleBinding;
import com.liadpaz.amp.interfaces.ItemTouchHelperAdapter;
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener;
import com.liadpaz.amp.interfaces.OnStartDragListener;
import com.liadpaz.amp.livedatautils.PlaylistsUtil;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.utils.Utilities;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistAdapter extends ListAdapter<Song, PlaylistAdapter.SongViewHolder> implements ItemTouchHelperAdapter {
    private static final String TAG = "AmpApp.PlaylistAdapter";
    private static final int TYPE_ITEM = 1;

    private ArrayList<Song> songs;

    private OnRecyclerItemClickListener onMoreClickListener;
    private View.OnClickListener onShuffleClickListener;
    private OnStartDragListener onStartDragListener;
    private ItemTouchHelperAdapter itemTouchHelperAdapter;

    private Context context;

    public PlaylistAdapter(@NonNull Context context, @NonNull OnRecyclerItemClickListener onMoreClickListener, @NonNull View.OnClickListener onShuffleClickListener, @NonNull ItemTouchHelperAdapter itemTouchHelperAdapter) {
        super(new DiffUtil.ItemCallback<Song>() {
            @Override
            public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) { return oldItem == newItem; }

            @Override
            public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) { return oldItem.equals(newItem); }
        });

        this.context = context;
        this.onMoreClickListener = onMoreClickListener;
        this.onShuffleClickListener = onShuffleClickListener;
        this.itemTouchHelperAdapter = itemTouchHelperAdapter;
    }

    public void setOnStartDragListener(@NonNull OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            return new SongViewHolder(ItemPlaylistSongBinding.inflate(LayoutInflater.from(context), parent, false), (v, position) -> {
                QueueUtil.setQueue(new ArrayList<>(songs));
                QueueUtil.setPosition(position - 1);
            }, onShuffleClickListener);
        }
        return new SongViewHolder(ItemSongShuffleBinding.inflate(LayoutInflater.from(context), parent, false), (v, position) -> {}, onShuffleClickListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        if (position != 0) {
            Song song = getItem(position - 1);

            ItemPlaylistSongBinding binding = (ItemPlaylistSongBinding)holder.binding;

            binding.tvSongName.setText(song.songTitle);
            binding.tvSongArtist.setText(Utilities.joinArtists(song.songArtists));

            Glide.with(context).load(Utilities.getCoverUri(song)).into(binding.ivSongCover);

            binding.btnMore.setOnClickListener(v -> onMoreClickListener.onItemClick(v, position - 1));
            binding.btnDrag.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onStartDragListener.onStartDrag(holder);
                }
                return true;
            });
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        PlaylistsUtil.setIsChanging(true);
        Collections.swap(songs, fromPosition - 1, toPosition - 1);
        itemTouchHelperAdapter.onItemMove(fromPosition - 1, toPosition - 1);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public void submitList(@Nullable List<Song> list) {
        super.submitList(songs = list != null ? new ArrayList<>(list) : null);
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private ViewBinding binding;

        SongViewHolder(@NonNull ViewBinding binding, @NonNull OnRecyclerItemClickListener onItemClick, @NonNull View.OnClickListener onShuffleClickListener) {
            super(binding.getRoot());
            this.binding = binding;

            if (binding instanceof ItemSongShuffleBinding) {
                ((ItemSongShuffleBinding)binding).getRoot().setOnClickListener(onShuffleClickListener);
            } else {
                itemView.setOnClickListener(v -> onItemClick.onItemClick(v, getAdapterPosition()));
            }
        }
    }
}
