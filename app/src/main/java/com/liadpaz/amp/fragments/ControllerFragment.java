package com.liadpaz.amp.fragments;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
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
import com.liadpaz.amp.service.ServiceConnector;
import com.liadpaz.amp.utils.Utilities;
import com.liadpaz.amp.viewmodels.CurrentSong;

public class ControllerFragment extends Fragment {
    private static final String TAG = "AmpApp.ControllerFragment";

    private boolean isPlaying = false;
    private MediaControllerCompat.TransportControls transportControls;

    private boolean isBright = false;

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
        ServiceConnector serviceConnector = ServiceConnector.getInstance();
        serviceConnector.playbackState.observe(getViewLifecycleOwner(), this::setPlayback);
        serviceConnector.nowPlaying.observe(getViewLifecycleOwner(), this::setMetadata);
        transportControls = serviceConnector.transportControls;

        binding.btnPlay.setOnClickListener(v -> {
            if (isPlaying) {
                transportControls.pause();
            } else {
                transportControls.play();
            }
        });

        ColorUtil.observe(this, color -> {
            if (isBright = Utilities.isColorBright(color)) {
                binding.tvSongArtist.setTextColor(Color.BLACK);
                binding.tvSongTitle.setTextColor(Color.BLACK);
                binding.btnPlay.setImageResource(isPlaying ? R.drawable.pause_pressed : R.drawable.play_pressed);
                binding.btnPlay.setImageTintList(ColorStateList.valueOf(Color.BLACK));
            } else {
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = getActivity().getTheme();
                theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
                TypedArray arr = getActivity().obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorPrimary});
                int primaryColor = arr.getColor(0, -1);
                arr.recycle();
                binding.tvSongArtist.setTextColor(primaryColor);
                binding.tvSongTitle.setTextColor(primaryColor);
                binding.btnPlay.setImageResource(isPlaying ? R.drawable.pause : R.drawable.play);
                binding.btnPlay.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            }
        });

        binding.getRoot().setOnClickListener(v -> BottomSheetBehavior.from(((MainActivity)requireActivity()).binding.extendedFragment)
                                                                     .setState(BottomSheetBehavior.STATE_EXPANDED));
    }

    private void setPlayback(@NonNull PlaybackStateCompat state) {
        isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        binding.btnPlay.setImageResource(state.getState() == PlaybackStateCompat.STATE_PLAYING ? R.drawable.pause : R.drawable.play);
        binding.btnPlay.setImageTintList(ColorStateList.valueOf(isBright ? Color.BLACK : Color.WHITE));
    }

    @SuppressWarnings("ConstantConditions")
    private void setMetadata(@NonNull MediaMetadataCompat metadata) {
        MediaDescriptionCompat description = metadata.getDescription();
        if (description != null) {
            try {
                Glide.with(this).load(description.getIconUri()).placeholder(R.drawable.song).into(binding.ivCurrentTrack);
                binding.setSong(new CurrentSong(description.getTitle().toString(), description.getSubtitle().toString()));
            } catch (Exception ignored) {
            }
        }
    }
}
