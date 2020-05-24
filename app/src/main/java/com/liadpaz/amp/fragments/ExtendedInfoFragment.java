package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.ExtendedInfoFragmentBinding;

public class ExtendedInfoFragment extends Fragment {
    private MediaControllerCompat controller;
    private MediaControllerCompat.Callback callback;

    private boolean isShowingQueue = false;

    private ExtendedInfoFragmentBinding binding;

    public ExtendedInfoFragment() { }

    public static ExtendedInfoFragment newInstance() { return new ExtendedInfoFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = ExtendedInfoFragmentBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        (controller = MainActivity.getController()).registerCallback(callback = new MediaControllerCompat.Callback() {
            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) { setMetadata(metadata); }
        });

        binding.tvCurrentSongName.setSelected(true);

        binding.btnQueue.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction().replace(R.id.layoutFragment, isShowingQueue ? ExtendedSongFragment.newInstance() : CurrentQueueFragment.newInstance()).commit();
            v.setBackgroundResource(isShowingQueue ? R.drawable.queue_music_not_shown : R.drawable.queue_music_shown);
            isShowingQueue = !isShowingQueue;
        });

        setMetadata(controller.getMetadata());
    }

    private void setMetadata(MediaMetadataCompat metadata) {
        if (metadata != null) {
            MediaDescriptionCompat description = metadata.getDescription();
            binding.tvCurrentSongName.setText(description.getTitle());
            binding.tvCurrentSongArtist.setText(description.getSubtitle());
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
