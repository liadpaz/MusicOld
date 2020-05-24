package com.liadpaz.amp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.FragmentExtendedBinding;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.utils.Utilities;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.lang.ref.WeakReference;

public class ExtendedFragment extends Fragment {
    private static final String TAG = "ExtendedFragment";

    private Handler handler;
    private Runnable runnable;
    private boolean shouldSeek = false;

    private MediaControllerCompat controller;
    private MediaControllerCompat.Callback callback;

    private long duration = 0;

    private boolean isUp = false;

    private FragmentExtendedBinding binding;

    public ExtendedFragment() { }

    public static ExtendedFragment newInstance() { return new ExtendedFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentExtendedBinding.inflate(inflater, container, false)).getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        handler = new SeekBarHandler(binding);

        (controller = MainActivity.getController()).registerCallback(callback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) { setPlayback(state); }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) { setMetadata(metadata); }

            @Override
            public void onRepeatModeChanged(int repeatMode) { setRepeat(repeatMode); }
        });

        setPlayback(controller.getPlaybackState());
        setMetadata(controller.getMetadata());
        setRepeat(controller.getRepeatMode());

        binding.btnSkipPrev.setOnClickListener(v -> controller.getTransportControls().skipToPrevious());
        binding.btnPlayPause.setOnClickListener(v -> {
            if (QueueUtil.queue.getValue().size() == 0) {
                QueueUtil.queue.setValue(LocalFiles.listSongs(requireContext()));
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.ACTION_QUEUE_POSITION, 0);
                controller.sendCommand(Constants.ACTION_QUEUE_POSITION, bundle, null);
            } else if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                controller.getTransportControls().pause();
            } else {
                controller.getTransportControls().play();
            }
        });
        binding.btnSkipNext.setOnClickListener(v -> controller.getTransportControls().skipToNext());
        binding.btnRepeat.setOnClickListener(v -> controller.getTransportControls().setRepeatMode(controller.getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ONE ? PlaybackStateCompat.REPEAT_MODE_ALL : PlaybackStateCompat.REPEAT_MODE_ONE));

        binding.sbSongProgress.setMax(1000);
        binding.sbSongProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                controller.getTransportControls().seekTo((int)((double)seekBar.getProgress() * duration / 1000));
            }
        });
        binding.tvTimeElapsed.setText(Utilities.formatTime(0));
        binding.tvTotalTime.setText(Utilities.formatTime(0));

        setMetadata(controller.getMetadata());
        setPlayback(controller.getPlaybackState());

        ((MainActivity)requireActivity()).binding.mainLayout.addPanelSlideListener(new SlidingUpPanelLayout.SimplePanelSlideListener() {
            @Override
            public void onPanelSlide(@NonNull View panel, float slideOffset) {
                if (slideOffset == 1.0) {
                    // show the info fragment
                    if (!isUp) {
                        getChildFragmentManager().beginTransaction().replace(R.id.infoFragment, ExtendedInfoFragment.newInstance()).commit();
                        isUp = true;
                    }
                } else if (slideOffset == 0) {
                    // show the controller fragment
                    if (isUp) {
                        getChildFragmentManager().beginTransaction().replace(R.id.infoFragment, ControllerFragment.newInstance()).replace(R.id.layoutFragment, ExtendedSongFragment.newInstance()).commit();
                        isUp = false;
                    }
                }
            }
        });

        getChildFragmentManager().beginTransaction().replace(R.id.infoFragment, ControllerFragment.newInstance()).replace(R.id.layoutFragment, ExtendedSongFragment.newInstance()).commit();

        ((MainActivity)requireActivity()).binding.mainLayout.setDragView(binding.infoFragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldSeek) {
            handler.post(runnable);
        }
    }

    private void setPlayback(PlaybackStateCompat state) {
        if (state == null) {
            binding.btnPlayPause.setBackgroundResource(R.drawable.play);
        } else {
            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                binding.btnPlayPause.setBackgroundResource(R.drawable.pause);
                updateSeekBar();
                shouldSeek = true;
            } else {
                binding.btnPlayPause.setBackgroundResource(R.drawable.play);
                shouldSeek = false;
            }
        }
    }

    private void setMetadata(MediaMetadataCompat metadata) {
        if (metadata != null) {
            binding.tvTimeElapsed.setText(Utilities.formatTime(0));
            binding.tvTotalTime.setText(Utilities.formatTime(duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
        }
    }

    private void setRepeat(int repeatMode) { binding.btnRepeat.setBackgroundResource(repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE ? R.drawable.repeat_one : R.drawable.repeat); }

    private void updateSeekBar() {
        handler.postDelayed(runnable = () -> {
            controller.sendCommand(Constants.ACTION_GET_POSITION, null, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    Message message = new Message();
                    message.arg1 = resultData.getInt(Constants.ACTION_GET_POSITION);
                    message.arg2 = resultData.getInt(Constants.EXTRA_TOTAL_TIME);
                    handler.dispatchMessage(message);
                }
            });
            if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                updateSeekBar();
            }
        }, 500);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (callback != null) {
            controller.unregisterCallback(callback);
        }
    }

    private static class SeekBarHandler extends Handler {
        private WeakReference<FragmentExtendedBinding> bindingWeakReference;

        SeekBarHandler(@NonNull FragmentExtendedBinding binding) {
            bindingWeakReference = new WeakReference<>(binding);
        }

        public void handleMessage(@NonNull Message msg) {
            SeekBar seekBar = bindingWeakReference.get().sbSongProgress;
            int current = msg.arg1;
            double total = msg.arg2;
            int seek = (int)(current / total * 1000);
            bindingWeakReference.get().tvTimeElapsed.setText(Utilities.formatTime(current));
            seekBar.setProgress(seek);
        }
    }
}
