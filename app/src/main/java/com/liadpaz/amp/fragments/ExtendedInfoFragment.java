package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentExtendedInfoBinding;
import com.liadpaz.amp.dialogs.NewPlaylistDialog;
import com.liadpaz.amp.dialogs.PlaylistsDialog;
import com.liadpaz.amp.LiveDataUtils.ColorUtil;
import com.liadpaz.amp.LiveDataUtils.QueueUtil;
import com.liadpaz.amp.utils.Utilities;

import java.util.ArrayList;

public class ExtendedInfoFragment extends Fragment {
    private static final String TAG = "AmpApp.ExtendedInfoFragment";

    private boolean isShowingQueue = false;
    private boolean isDark = false;

    private FragmentExtendedInfoBinding binding;

    public ExtendedInfoFragment() { }

    @NonNull
    public static ExtendedInfoFragment newInstance() { return new ExtendedInfoFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentExtendedInfoBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnQueue.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction().replace(R.id.layoutFragment, isShowingQueue ? ExtendedSongFragment.newInstance() : CurrentQueueFragment.newInstance()).commit();
            v.setBackgroundResource(isShowingQueue ? (isDark ? R.drawable.queue_music_not_shown_black : R.drawable.queue_music_not_shown) : R.drawable.queue_music_shown);
            isShowingQueue = !isShowingQueue;
        });
        binding.btnMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.inflate(R.menu.menu_queue);

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menuQueueClear: {
                        QueueUtil.queue.postValue(new ArrayList<>());
                        MainActivity.getController().getTransportControls().stop();
                        break;
                    }

                    case R.id.menuQueueSavePlaylist: {
                        new NewPlaylistDialog(QueueUtil.queue.getValue()).show(getChildFragmentManager(), null);
                        break;
                    }

                    case R.id.menuQueueAddPlaylist: {
                        new PlaylistsDialog(QueueUtil.queue.getValue()).show(getChildFragmentManager(), null);
                        break;
                    }
                }
                return true;
            });

            popupMenu.show();
        });
        binding.ivDrop.setOnClickListener(v -> BottomSheetBehavior.from(((MainActivity)requireActivity()).binding.extendedFragment).setState(BottomSheetBehavior.STATE_COLLAPSED));

        ColorUtil.observe(this, color -> {
            if (isDark = Utilities.isColorBright(color)) {
                binding.btnMore.setBackgroundResource(R.drawable.more_black);
                binding.btnQueue.setBackgroundResource(isShowingQueue ? R.drawable.queue_music_shown : R.drawable.queue_music_not_shown_black);
                binding.ivDrop.setImageResource(R.drawable.arrow_down_black);
            } else {
                binding.btnMore.setBackgroundResource(R.drawable.more);
                binding.btnQueue.setBackgroundResource(isShowingQueue ? R.drawable.queue_music_shown : R.drawable.queue_music_not_shown);
                binding.ivDrop.setImageResource(R.drawable.arrow_down);
            }
        });
    }
}
