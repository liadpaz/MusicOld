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
import com.liadpaz.amp.databinding.FragmentExtendedBinding;

public class ExtendedSongFragment extends Fragment {

    private MediaControllerCompat controller;
    private MediaControllerCompat.Callback callback;

    private FragmentExtendedBinding binding;

    public ExtendedSongFragment() { }

    @NonNull
    public static ExtendedSongFragment newInstance() { return new ExtendedSongFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentExtendedBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        (controller = MainActivity.getController()).registerCallback(callback = new MediaControllerCompat.Callback() {
            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                setMetadata(metadata);
            }
        });

        setMetadata(controller.getMetadata());
    }

    private void setMetadata(MediaMetadataCompat metadata) {
        if (metadata != null) {
            MediaDescriptionCompat description = metadata.getDescription();
            if (description.getIconUri() != null) {
                binding.ivSongCover.setImageURI(description.getIconUri());
            } else {
                binding.ivSongCover.setImageResource(R.drawable.song);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callback != null) {
            controller.unregisterCallback(callback);
        }
    }
}
