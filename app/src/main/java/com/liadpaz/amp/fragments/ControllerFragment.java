package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentControllerBinding;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.viewmodels.CurrentSong;

public class ControllerFragment extends Fragment {

    private MediaControllerCompat controller;
    private MediaControllerCompat.Callback callback;

    private FragmentControllerBinding binding;

    public ControllerFragment() { }

    @NonNull
    public static ControllerFragment newInstance() { return new ControllerFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentControllerBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        (controller = MainActivity.getController()).registerCallback(callback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) { setPlayback(state); }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) { setMetadata(metadata); }
        });

        binding.btnPlay.setOnClickListener(v -> {
            MediaControllerCompat controller = MainActivity.getController();
            if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) {
                QueueUtil.queue.postValue(LocalFiles.listSongsByName(requireContext()));
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.ACTION_QUEUE_POSITION, 0);
                controller.sendCommand(Constants.ACTION_QUEUE_POSITION, bundle, null);
            } else if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                controller.getTransportControls().pause();
            } else {
                controller.getTransportControls().play();
            }
        });

        setPlayback(controller.getPlaybackState());
        setMetadata(controller.getMetadata());
    }

    private void setPlayback(PlaybackStateCompat state) {
        if (state != null) {
            binding.btnPlay.setBackgroundResource(state.getState() == PlaybackStateCompat.STATE_PLAYING ? R.drawable.pause : R.drawable.play);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setMetadata(MediaMetadataCompat metadata) {
        if (metadata != null) {
            MediaDescriptionCompat description = metadata.getDescription();
            Glide.with(this).load(description.getIconUri()).placeholder(R.drawable.song).into(binding.ivCurrentTrack);
            binding.setSong(new CurrentSong(description.getTitle().toString(), description.getSubtitle().toString()));
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
