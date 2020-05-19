package com.liadpaz.amp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.liadpaz.amp.databinding.ActivityExtendedSongBinding;
import com.liadpaz.amp.fragments.CurrentQueueFragment;
import com.liadpaz.amp.fragments.ExtendedSongFragment;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.utils.Utilities;

import java.lang.ref.WeakReference;

public class ExtendedSongActivity extends AppCompatActivity {

    private static final String TAG = "EXTENDED_SONG_ACTIVITY";

    private Handler handler;
    private Runnable runnable;
    private boolean shouldSeek = false;

    private MediaControllerCompat controller;
    private MediaControllerCompat.Callback callback;

    private ActivityExtendedSongBinding binding;

    private boolean isShowingQueue = false;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExtendedSongBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvCurrentSongName.setSelected(true);

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
                QueueUtil.queue.setValue(LocalFiles.listSongs(this));
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
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.ACTION_SEEK_TO, seekBar.getProgress());
                controller.sendCommand(Constants.ACTION_SEEK_TO, bundle, null);
            }
        });

        binding.btnQueue.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.layoutExtended, isShowingQueue ? ExtendedSongFragment.newInstance() : CurrentQueueFragment.newInstance()).commit();
            v.setBackgroundResource(isShowingQueue ? R.drawable.queue_music_not_shown : R.drawable.queue_music_shown);
            isShowingQueue = !isShowingQueue;
        });
        binding.tvTimeElapsed.setText(Utilities.formatTime(0));
        binding.tvTotalTime.setText(Utilities.formatTime(0));

        getSupportFragmentManager().beginTransaction().replace(R.id.layoutExtended, ExtendedSongFragment.newInstance()).commit();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
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
        if (metadata == null) {
            binding.tvCurrentSongName.setText(null);
            binding.tvCurrentSongArtist.setText(null);
        } else {
            MediaDescriptionCompat description = metadata.getDescription();
            binding.tvCurrentSongName.setText(description.getTitle());
            binding.tvCurrentSongArtist.setText(description.getSubtitle());
            binding.tvTimeElapsed.setText(Utilities.formatTime(0));
            binding.tvTotalTime.setText(Utilities.formatTime(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
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
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (callback != null) {
            controller.unregisterCallback(callback);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_down_reverse, R.anim.slide_up_reverse);
    }

    private static class SeekBarHandler extends Handler {
        private WeakReference<ActivityExtendedSongBinding> bindingWeakReference;

        SeekBarHandler(ActivityExtendedSongBinding binding) {
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
