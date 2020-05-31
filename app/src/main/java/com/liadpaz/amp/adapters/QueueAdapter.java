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
import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.ItemQueueSongBinding;
import com.liadpaz.amp.interfaces.ItemTouchHelperAdapter;
import com.liadpaz.amp.interfaces.OnRecyclerItemClickListener;
import com.liadpaz.amp.interfaces.OnStartDragListener;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.utils.Utilities;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueueAdapter extends ListAdapter<Song, QueueAdapter.SongViewHolder> implements ItemTouchHelperAdapter {
    private static final String TAG = "QUEUE_ADAPTER";

    private ArrayList<Song> songs;

    private OnStartDragListener onStartDragListener;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;
    private ItemTouchHelperAdapter itemTouchHelperAdapter;

    private Context context;

    private int queuePosition;

    public QueueAdapter(@NonNull Fragment fragment, @NonNull OnRecyclerItemClickListener onRecyclerItemClickListener, @NonNull ItemTouchHelperAdapter itemTouchHelperAdapter) {
        super(new DiffUtil.ItemCallback<Song>() {
            @Override
            public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) { return oldItem == newItem; }

            @Override
            public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) { return oldItem.equals(newItem); }
        });
        this.context = fragment.requireContext();
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
        this.itemTouchHelperAdapter = itemTouchHelperAdapter;

        QueueUtil.queuePosition.observe(fragment, queuePosition -> this.queuePosition = queuePosition);
    }

    public void setOnStartDragListener(@NonNull OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new QueueAdapter.SongViewHolder(ItemQueueSongBinding.inflate(LayoutInflater.from(context), parent, false), onRecyclerItemClickListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final SongViewHolder holder, final int position) {
        Song song = getItem(position);

        holder.binding.tvSongTitle.setText(song.songTitle);
        holder.binding.tvSongArtist.setText(Utilities.joinArtists(song.songArtists));
        Glide.with(context).load(Utilities.getCoverUri(song)).placeholder(R.drawable.song).into(holder.binding.ivSongCover);

        holder.binding.btnDrag.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                onStartDragListener.onStartDrag(holder);
            }
            return true;
        });
    }

    public void onItemDismiss(int position) {
        songs.remove(position);
        if (queuePosition > position) {
            QueueUtil.addToPosition(-1);
        }
        itemTouchHelperAdapter.onItemDismiss(position);
        notifyItemRemoved(position);
        Toast.makeText(context, String.format("%s %s", getItem(position).songTitle, context.getString(R.string.queue_removed)), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemMove(final int fromPosition, final int toPosition) {
        Collections.swap(songs, fromPosition, toPosition);
        if (queuePosition == fromPosition) {
            QueueUtil.setPosition(toPosition);
        } else if (queuePosition > fromPosition && queuePosition < toPosition) {
            QueueUtil.addToPosition(1);
        } else if (queuePosition < fromPosition && queuePosition > toPosition) {
            QueueUtil.addToPosition(-1);
        }
        itemTouchHelperAdapter.onItemMove(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void submitList(@Nullable List<Song> list) {
        super.submitList(songs = list != null ? new ArrayList<>(list) : null);
    }

    public ArrayList<Song> getQueue() { return songs; }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private ItemQueueSongBinding binding;

        SongViewHolder(@NonNull ItemQueueSongBinding binding, OnRecyclerItemClickListener onRecyclerItemClickListener) {
            super(binding.getRoot());

            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                QueueUtil.setPosition(position);
                MainActivity.getController().sendCommand(Constants.ACTION_QUEUE_POSITION, null, null);
            });

            binding.btnMore.setOnClickListener(v -> onRecyclerItemClickListener.onItemClick(v, getAdapterPosition()));
        }
    }
}
