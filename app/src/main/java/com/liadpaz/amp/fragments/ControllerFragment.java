package com.liadpaz.amp.fragments;

import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentControllerBinding;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.viewmodels.CurrentSong;

public class ControllerFragment extends Fragment {

    private MediaController controller;
    private MediaController.Callback callback;

    private FragmentControllerBinding binding;

    public ControllerFragment() { }

    @NonNull
    public static ControllerFragment newInstance() { return new ControllerFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentControllerBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        (controller = MainActivity.getController()).registerCallback(callback = new MediaController.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackState state) { setPlayback(state); }

            @Override
            public void onMetadataChanged(MediaMetadata metadata) { setMetadata(metadata); }
        });

        binding.btnPlay.setOnClickListener(v -> {
            if (QueueUtil.queue.getValue().size() == 0) {
                QueueUtil.queue.setValue(LocalFiles.listSongsByName(requireContext()));
                QueueUtil.setPosition(0);
                controller.sendCommand(Constants.ACTION_QUEUE_POSITION, null, null);
            } else if (controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                controller.getTransportControls().pause();
            } else {
                controller.getTransportControls().play();
            }
        });

        setPlayback(controller.getPlaybackState());
        setMetadata(controller.getMetadata());

        binding.getRoot().setOnClickListener(v -> BottomSheetBehavior.from(((MainActivity)requireActivity()).binding.extendedFragment).setState(BottomSheetBehavior.STATE_EXPANDED));
    }

    private void setPlayback(PlaybackState state) {
        if (state != null) {
            binding.btnPlay.setBackgroundResource(state.getState() == PlaybackState.STATE_PLAYING ? R.drawable.pause : R.drawable.play);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setMetadata(MediaMetadata metadata) {
        if (metadata != null) {
            MediaDescription description = metadata.getDescription();
            if (description != null) {
                try {
                    Glide.with(this).load(description.getIconUri()).placeholder(R.drawable.song).into(binding.ivCurrentTrack);
                    binding.setSong(new CurrentSong(description.getTitle().toString(), description.getSubtitle().toString()));
                } catch (Exception ignored) {
                }
            }
        } else {
            binding.setSong(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (callback != null) {
            controller.unregisterCallback(callback);
        }
    }
}
