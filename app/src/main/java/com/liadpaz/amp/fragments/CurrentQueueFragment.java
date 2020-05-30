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
import com.liadpaz.amp.utils.QueueUtil;

public class CurrentQueueFragment extends Fragment {
    private static final String TAG = "QUEUE_FRAGMENT";

    private QueueAdapter adapter;

    private FragmentCurrentQueueBinding binding;

    public CurrentQueueFragment() { }

    @NonNull
    public static CurrentQueueFragment newInstance() { return new CurrentQueueFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
                        new PlaylistsDialog(requireContext(), adapter.getCurrentList().get(position)).show();
                        break;
                    }
                }
                return true;
            });
            popupMenu.show();
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() { //ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END) {
            boolean isSwiping = false;

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int moveFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = isSwiping || viewHolder.getAdapterPosition() == QueueUtil.queuePosition.getValue() ? 0 : (ItemTouchHelper.START | ItemTouchHelper.END);
                return makeMovementFlags(moveFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.onItemDismiss(viewHolder.getAdapterPosition());
            }
        });
        binding.rvQueue.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvQueue.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        binding.rvQueue.setAdapter(adapter);
        itemTouchHelper.attachToRecyclerView(binding.rvQueue);

        adapter.setOnStartDragListener(itemTouchHelper::startDrag);

        binding.rvQueue.scrollToPosition(QueueUtil.queuePosition.getValue());

        QueueUtil.queue.observe(requireActivity(), adapter::submitList);

        registerForContextMenu(binding.rvQueue);
    }
}