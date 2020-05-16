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
import com.liadpaz.amp.fragments.ExtendedSongFragment;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
import com.liadpaz.amp.utils.MinutesSeconds;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class ExtendedSongActivity extends AppCompatActivity {

    private static final String TAG = "EXTENDED_SONG_ACTIVITY";

    @SuppressWarnings("FieldCanBeLocal")
    private Timer timer;
    private Handler handler;

    private MediaControllerCompat controller;

    private MediaControllerCompat.Callback callback;

    private ActivityExtendedSongBinding binding;

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
            if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) {
                LocalFiles.setQueue(LocalFiles.listSongs(this));
                controller.sendCommand(Constants.ACTION_SET_QUEUE, null, null);
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
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.ACTION_SEEK_TO, progress);
                    controller.sendCommand(Constants.ACTION_SEEK_TO, bundle, null);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.layoutExtended, ExtendedSongFragment.newInstance()).commit();
    }

    private void setPlayback(PlaybackStateCompat state) {
        if (state == null) {
            binding.btnPlayPause.setBackgroundResource(R.drawable.play);
        } else {
            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                binding.btnPlayPause.setBackgroundResource(R.drawable.pause);

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(() -> controller.sendCommand(Constants.ACTION_GET_POSITION, null, new ResultReceiver(new Handler()) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                Message message = new Message();
                                message.arg1 = resultData.getInt(Constants.ACTION_GET_POSITION);
                                message.arg2 = resultData.getInt(Constants.EXTRA_TOTAL_TIME);
                                handler.dispatchMessage(message);
                            }
                        }));
                    }
                }, 0, 500);
            } else {
                timer = new Timer();
                binding.btnPlayPause.setBackgroundResource(R.drawable.play);
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
            binding.tvTotalTime.setText(MinutesSeconds.format(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
        }
    }

    private void setRepeat(int repeatMode) { binding.btnRepeat.setBackgroundResource(repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE ? R.drawable.repeat_one : R.drawable.repeat); }

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_down_reverse, R.anim.slide_up_reverse);
    }

    private static class SeekBarHandler extends Handler {
        private WeakReference<ActivityExtendedSongBinding> bindingWeakReference;

        SeekBarHandler(ActivityExtendedSongBinding binding) {
            super();

            bindingWeakReference = new WeakReference<>(binding);
        }

        public void handleMessage(@NonNull Message msg) {
            SeekBar seekBar = bindingWeakReference.get().sbSongProgress;
            long current = msg.arg1;
            double total = msg.arg2;
            int seek = (int)(current / total * 1000);
            bindingWeakReference.get().tvTimeElapsed.setText(MinutesSeconds.format(current));
            seekBar.setProgress(seek);
        }
    }
}
