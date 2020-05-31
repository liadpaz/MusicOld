package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.liadpaz.amp.R;
import com.liadpaz.amp.adapters.QueueAdapter;
import com.liadpaz.amp.databinding.FragmentCurrentQueueBinding;
import com.liadpaz.amp.dialogs.PlaylistsDialog;
import com.liadpaz.amp.interfaces.ItemTouchHelperAdapter;
import com.liadpaz.amp.utils.QueueUtil;

public class CurrentQueueFragment extends Fragment {
    private static final String TAG = "QUEUE_FRAGMENT";

    private boolean isChanging = false;

    private QueueAdapter adapter;

    private FragmentCurrentQueueBinding binding;

    private CurrentQueueFragment() { }

    @NonNull
    public static CurrentQueueFragment newInstance() { return new CurrentQueueFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentCurrentQueueBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new QueueAdapter(this, (v, position) -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.inflate(R.menu.menu_queue_song);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menuQueueRemove: {
                        if (position != QueueUtil.queuePosition.getValue()) {
                            adapter.onItemDismiss(position);
                        }
                        break;
                    }

                    case R.id.menuQueueAddPlaylist: {
                        new PlaylistsDialog(adapter.getCurrentList().get(position)).show(getChildFragmentManager(), null);
                        break;
                    }
                }
                return true;
            });
            popupMenu.show();
        }, new ItemTouchHelperAdapter() {
            @Override
            public void onItemMove(int fromPosition, int toPosition) {
                isChanging = true;
                QueueUtil.queue.postValue(adapter.getQueue());
            }

            @Override
            public void onItemDismiss(int position) {
                isChanging = true;
                QueueUtil.queue.postValue(adapter.getQueue());
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            private boolean isSwiping = false;

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int moveFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = isSwiping || viewHolder.getAdapterPosition() == QueueUtil.queuePosition.getValue() ? 0 : (ItemTouchHelper.START | ItemTouchHelper.END);
                return makeMovementFlags(moveFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                isSwiping = true;
                adapter.onItemDismiss(viewHolder.getAdapterPosition());
                isSwiping = false;
            }
        });
        binding.rvQueue.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvQueue.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.rvQueue.setAdapter(adapter);
        itemTouchHelper.attachToRecyclerView(binding.rvQueue);

        adapter.setOnStartDragListener(itemTouchHelper::startDrag);

        binding.rvQueue.scrollToPosition(QueueUtil.queuePosition.getValue());

        QueueUtil.queue.observe(getViewLifecycleOwner(), songs -> {
            if (!isChanging) {
                adapter.submitList(songs);
            } else {
                isChanging = false;
            }
        });

        registerForContextMenu(binding.rvQueue);
    }
}
