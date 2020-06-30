package com.liadpaz.amp.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.liadpaz.amp.livedatautils.ColorUtil;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.utils.Utilities;

public class ExtendedInfoFragment extends Fragment {
    private static final String TAG = "AmpApp.ExtendedInfoFragment";

    private boolean isShowingQueue = false;
    private boolean isBright = false;

    private FragmentExtendedInfoBinding binding;

    public ExtendedInfoFragment() { }

    @NonNull
    public static ExtendedInfoFragment newInstance() { return new ExtendedInfoFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentExtendedInfoBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnQueue.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction().replace(R.id.layoutFragment, isShowingQueue ? ExtendedViewPagerFragment.newInstance() : CurrentQueueFragment.newInstance()).commit();
            ((ImageButton)v).setImageTintList(isShowingQueue ? ColorStateList.valueOf(isBright ? Color.BLACK : Color.WHITE) : ColorStateList.valueOf(Color.BLUE));
            isShowingQueue = !isShowingQueue;
        });
        binding.btnMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.inflate(R.menu.menu_queue);

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menuQueueClear: {
//                        MainActivity.getController().sendCommand(Constants.ACTION_RESET, null, null);
                        // TODO: make clear queue
                        break;
                    }

                    case R.id.menuQueueSavePlaylist: {
                        new NewPlaylistDialog(QueueUtil.getQueue()).show(getChildFragmentManager(), null);
                        break;
                    }

                    case R.id.menuQueueAddPlaylist: {
                        new PlaylistsDialog(QueueUtil.getQueue()).show(getChildFragmentManager(), null);
                        break;
                    }
                }
                return true;
            });

            popupMenu.show();
        });
        binding.ivDrop.setOnClickListener(v -> BottomSheetBehavior.from(((MainActivity)requireActivity()).binding.extendedFragment).setState(BottomSheetBehavior.STATE_COLLAPSED));

        ColorUtil.observe(this, color -> {
            if (isBright = Utilities.isColorBright(color)) {
                binding.btnMore.setImageTintList(ColorStateList.valueOf(Color.BLACK));
                binding.btnQueue.setImageTintList(isShowingQueue ? ColorStateList.valueOf(Color.BLUE) : ColorStateList.valueOf(Color.BLACK));
                binding.ivDrop.setImageTintList(ColorStateList.valueOf(Color.BLACK));
            } else {
                binding.btnMore.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                binding.btnQueue.setImageTintList(isShowingQueue ? ColorStateList.valueOf(Color.BLUE) : ColorStateList.valueOf(Color.WHITE));
                binding.ivDrop.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            }
        });
    }
}
