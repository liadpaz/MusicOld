package com.liadpaz.amp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.ItemQueueSongBinding;
import com.liadpaz.amp.interfaces.ItemTouchHelperAdapter;
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener;
import com.liadpaz.amp.interfaces.OnStartDragListener;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.service.ServiceConnector;
import com.liadpaz.amp.utils.Utilities;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueueAdapter extends ListAdapter<Song, QueueAdapter.SongViewHolder> implements ItemTouchHelperAdapter {
    private static final String TAG = "AmpApp.QueueAdapter";

    private OnStartDragListener onStartDragListener;
    private OnRecyclerItemClickListener onMoreClickListener;
    private ItemTouchHelperAdapter itemTouchHelperAdapter;

    private ConcatenatingMediaSource mediaSource;

    private List<Song> songs = new ArrayList<>();

    private Context context;

    public QueueAdapter(@NonNull Fragment fragment, @NonNull OnRecyclerItemClickListener onMoreClickListener, @NonNull ItemTouchHelperAdapter itemTouchHelperAdapter) {
        super(new DiffUtil.ItemCallback<Song>() {
            @Override
            public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) { return oldItem == newItem; }

            @Override
            public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) { return oldItem.equals(newItem); }
        });
        this.context = fragment.requireContext();
        this.onMoreClickListener = onMoreClickListener;
        this.itemTouchHelperAdapter = itemTouchHelperAdapter;

        ServiceConnector.getInstance().mediaSource.observeForever(mediaSource -> this.mediaSource = mediaSource);
    }

    public void setOnStartDragListener(@NonNull OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new QueueAdapter.SongViewHolder(ItemQueueSongBinding.inflate(LayoutInflater.from(context), parent, false), (v, position) -> ServiceConnector.getInstance().transportControls.skipToQueueItem(position), (v, position) -> onMoreClickListener.onItemClick(v, position));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final SongViewHolder holder, final int position) {
        Song song = getItem(position);

        ItemQueueSongBinding binding = holder.binding;

        binding.tvSongTitle.setText(song.title);
        binding.tvSongArtist.setText(Utilities.joinArtists(song.artists));

        Glide.with(context).load(song.getCoverUri()).placeholder(R.drawable.song).into(binding.ivSongCover);

        binding.btnDrag.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                onStartDragListener.onStartDrag(holder);
            }
            return true;
        });
    }

    public void onItemDismiss(int position) {
        songs.remove(position);
        QueueUtil.setIsChanging(true);
        QueueUtil.setQueue(songs);
        mediaSource.removeMediaSource(position);
        itemTouchHelperAdapter.onItemDismiss(position);
        notifyItemRemoved(position);
        Toast.makeText(context, context.getString(R.string.queue_removed, getItem(position).title), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemMove(final int fromPosition, final int toPosition) {
        Collections.swap(songs, fromPosition, toPosition);
        QueueUtil.setIsChanging(true);
        QueueUtil.setQueue(songs);
        mediaSource.moveMediaSource(fromPosition, toPosition);
        itemTouchHelperAdapter.onItemMove(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void submitList(@Nullable List<Song> list) {
        super.submitList(songs = list != null ? new ArrayList<>(list) : null);
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private ItemQueueSongBinding binding;

        SongViewHolder(@NonNull ItemQueueSongBinding binding, @NonNull OnRecyclerItemClickListener onItemClickListener, @NonNull OnRecyclerItemClickListener onMoreClickListener) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, getAdapterPosition()));
            binding.btnMore.setOnClickListener(v -> onMoreClickListener.onItemClick(v, getAdapterPosition()));
        }
    }
}
