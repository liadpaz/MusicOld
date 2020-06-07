package com.liadpaz.amp.fragments;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.util.TypedValue;
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
import com.liadpaz.amp.livedatautils.ColorUtil;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.livedatautils.SongsUtil;
import com.liadpaz.amp.utils.Utilities;
import com.liadpaz.amp.viewmodels.CurrentSong;

public class ControllerFragment extends Fragment {
    private static final String TAG = "AmpApp.ControllerFragment";

    private MediaController controller;
    private MediaController.Callback callback;

    private boolean isDark = false;

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
            if (QueueUtil.getQueueSize() == 0) {
                QueueUtil.setQueue(SongsUtil.getSongs());
                QueueUtil.setPosition(0);
            } else if (controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                controller.getTransportControls().pause();
            } else {
                controller.getTransportControls().play();
            }
        });

        setPlayback(controller.getPlaybackState());
        setMetadata(controller.getMetadata());

        ColorUtil.observe(this, color -> {
            if (isDark = Utilities.isColorBright(color)) {
                binding.tvSongArtist.setTextColor(Color.BLACK);
                binding.tvSongTitle.setTextColor(Color.BLACK);
                binding.btnPlay.setBackgroundResource(controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING ? R.drawable.pause_black : R.drawable.play_black);
            } else {
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = getActivity().getTheme();
                theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
                TypedArray arr = getActivity().obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorPrimary});
                int primaryColor = arr.getColor(0, -1);
                arr.recycle();
                binding.tvSongArtist.setTextColor(primaryColor);
                binding.tvSongTitle.setTextColor(primaryColor);
                binding.btnPlay.setBackgroundResource(controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING ? R.drawable.pause : R.drawable.play);
            }
        });

        binding.getRoot().setOnClickListener(v -> BottomSheetBehavior.from(((MainActivity)requireActivity()).binding.extendedFragment).setState(BottomSheetBehavior.STATE_EXPANDED));
    }

    private void setPlayback(PlaybackState state) {
        if (state != null) {
            binding.btnPlay.setBackgroundResource(state.getState() == PlaybackState.STATE_PLAYING ? (isDark ? R.drawable.pause_black : R.drawable.pause) : (isDark ? R.drawable.play_black : R.drawable.play));
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
