package com.liadpaz.amp.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.notification.MediaNotification;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.QueueUtil;
import com.liadpaz.amp.utils.Song;
import com.liadpaz.amp.utils.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class MediaPlayerService extends MediaBrowserServiceCompat {
    @SuppressWarnings("unused")
    private static final String TAG = "MUSIC_SERVICE";
    private static final String LOG_TAG = "MEDIA_SESSION_LOG";
    private static final int NOTIFICATION_ID = 273;

    private static final String CHANNEL_ID = "music_channel";

    private final Object lock = new Object();

    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;
    private BecomingNoisyReceiver becomingNoisyReceiver;

    private MediaMetadataCompat.Builder metadataBuilder;
    private PlaybackStateCompat.Builder playbackBuilder;

    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private AudioAttributes audioAttributes;
    private boolean resumeOnFocusGain = false;

    private Song currentSource;
    private int queuePosition = 0;
    private ArrayList<Song> queue = new ArrayList<>();

    private void setQueuePosition(int position) {
        QueueUtil.setPosition(queuePosition = position);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            if (!mediaPlayer.isLooping()) {
                mediaSession.getController().getTransportControls().skipToNext();
            }
        });

        QueueUtil.queue.observeForever(songs -> {
            this.queue = songs;
            if (this.queue.size() > 0 && currentSource == null) {
                setSource(queue.get(0));
            }
        });
        QueueUtil.queuePosition.observeForever(queuePosition -> this.queuePosition = queuePosition);

        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build();

        mediaPlayer.setAudioAttributes(audioAttributes);

        mediaSession = new MediaSessionCompat(this, LOG_TAG);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setSessionActivity(PendingIntent.getActivity(this, 10, new Intent(this, MainActivity.class), 0));

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onCommand(String command, Bundle extras, ResultReceiver cb) {
                switch (command) {
                    case Constants.ACTION_QUEUE_POSITION: {
                        setSource(queue.get(queuePosition = extras.getInt(Constants.ACTION_QUEUE_POSITION)));
                        onPlay();
                        break;
                    }

                    case Constants.ACTION_GET_POSITION: {
                        try {
                            Bundle bundle = new Bundle();
                            bundle.putInt(Constants.ACTION_GET_POSITION, mediaPlayer.getCurrentPosition());
                            bundle.putInt(Constants.EXTRA_TOTAL_TIME, mediaPlayer.getDuration());
                            cb.send(1, bundle);
                        } catch (Exception ignored) {
                        }
                        break;
                    }

                    case Constants.ACTION_SEEK_TO: {
                        onSeekTo((int)((double)extras.getInt(Constants.ACTION_SEEK_TO) * mediaPlayer.getDuration() / 1000));
                        break;
                    }
                }
            }

            @Override
            public void onPlay() {
                if (audioManager.requestAudioFocus(audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setOnAudioFocusChangeListener(focusChange -> {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN: {
                            if (resumeOnFocusGain) {
                                synchronized (lock) {
                                    resumeOnFocusGain = false;
                                }
                                mediaPlayer.setVolume(1F, 1F);
                                play();
                            }
                            break;
                        }

                        case AudioManager.AUDIOFOCUS_LOSS: {
                            synchronized (lock) {
                                resumeOnFocusGain = false;
                            }
                            pause();
                            break;
                        }

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                            mediaPlayer.setVolume(0.2F, 0.2F);
                            break;
                        }

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                            synchronized (lock) {
                                resumeOnFocusGain = mediaPlayer.isPlaying();
                            }
                            pause();
                            break;
                        }
                    }
                }).setAudioAttributes(audioAttributes).build()) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mediaPlayer.setVolume(1F, 1F);
                    play();
                }
            }

            @Override
            public void onPause() {
                pause();
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
                resumeOnFocusGain = false;
            }

            @Override
            public void onStop() {
                resumeOnFocusGain = false;
                mediaPlayer.stop();
                mediaSession.setActive(false);
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
                sendPlaybackState();
                stopSelf();
            }

            @Override
            public void onSkipToNext() {
                if (mediaSession.getController().getPlaybackState().getState() != PlaybackStateCompat.STATE_NONE) {
                    if (queue.size() > 0) {
                        if (++queuePosition == queue.size()) {
                            setQueuePosition(queuePosition = 0);
                        } else {
                            setQueuePosition(queuePosition);
                        }
                        setSource(queue.get(queuePosition));
                        onPlay();
                    }
                }
            }

            @Override
            public void onSkipToPrevious() {
                if (mediaSession.getController().getPlaybackState().getState() != PlaybackStateCompat.STATE_NONE) {
                    if (TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()) > 2) {
                        onSeekTo(0);
                    } else if (queue.size() > 0) {
                        if (--queuePosition == -1) {
                            setQueuePosition(queuePosition = queue.size() - 1);
                        } else {
                            setQueuePosition(queuePosition);
                        }
                        setSource(queue.get(queuePosition));
                        onPlay();
                    }
                }
            }

            @Override
            public void onSeekTo(long pos) {
                if (mediaSession.getController().getPlaybackState().getState() != PlaybackStateCompat.STATE_NONE) {
                    boolean returnToPlay = mediaPlayer.isPlaying();
                    mediaPlayer.pause();
                    mediaPlayer.seekTo((int)pos);
                    if (returnToPlay) {
                        mediaPlayer.start();
                    }
                    sendPlaybackState();
                }
            }

            @Override
            public void onSetRepeatMode(int repeatMode) {
                mediaPlayer.setLooping(repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE);
                mediaSession.setRepeatMode(repeatMode);
            }
        });

        metadataBuilder = new MediaMetadataCompat.Builder();
        mediaSession.setMetadata(null);

        playbackBuilder = new PlaybackStateCompat.Builder();
        mediaSession.setPlaybackState(playbackBuilder.setState(PlaybackStateCompat.STATE_NONE, 0, 0).build());

        startForeground(NOTIFICATION_ID, new Notification.Builder(this, CHANNEL_ID).build());
        stopForeground(true);

        setSessionToken(mediaSession.getSessionToken());

        becomingNoisyReceiver = new BecomingNoisyReceiver(this, mediaSession.getController());
    }

    private void play() {
        resumeOnFocusGain = true;
        becomingNoisyReceiver.register();
        mediaPlayer.start();
        sendPlaybackState();
        startNotification(true);
    }

    private void pause() {
        becomingNoisyReceiver.unregister();
        mediaPlayer.pause();
        sendPlaybackState();
        startNotification(false);
    }

    private void sendMetadata(@NonNull Song song) {
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_TITLE, song.getSongTitle()).putText(MediaMetadataCompat.METADATA_KEY_ARTIST, Utilities.joinArtists(song.getSongArtists())).putText(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbum()).putString(MediaMetadataCompat.METADATA_KEY_ART_URI, Utilities.getCoverUri(song).toString()).putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration());
        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void sendPlaybackState() {
        playbackBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SET_REPEAT_MODE).setState(mediaPlayer.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition(), 1F);
        mediaSession.setPlaybackState(playbackBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return START_STICKY;
    }

    private void setSource(Song song) {
        try {
            if (audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, (currentSource = song).getSongId()));
            mediaPlayer.prepare();
            sendMetadata(currentSource);
        } catch (Exception ignored) {
        }
    }

    private void startNotification(final boolean isPlaying) {
        NotificationCompat.Builder builder = MediaNotification.from(this, mediaSession, isPlaying);
        Glide.with(this).asBitmap().load(Utilities.getCoverUri(currentSource)).placeholder(R.drawable.song).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap cover, @Nullable Transition<? super Bitmap> transition) {
                startForeground(NOTIFICATION_ID, builder.setLargeIcon(cover).build());
                if (!isPlaying) {
                    stopForeground(false);
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                Bitmap cover = Utilities.getBitmapFromVectorDrawable(MediaPlayerService.this, R.drawable.song_color);

                startForeground(NOTIFICATION_ID, builder.setLargeIcon(cover).build());
                if (!isPlaying) {
                    stopForeground(false);
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {}
        });
    }

    @NonNull
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) { return new BrowserRoot(getString(R.string.app_name), null); }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) { result.sendResult(null); }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaSession.release();
    }

    private static class BecomingNoisyReceiver extends BroadcastReceiver {
        private IntentFilter noisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        private Context context;
        private MediaControllerCompat controller;

        private boolean registered = false;

        public BecomingNoisyReceiver(@NonNull Context context, @NonNull MediaControllerCompat controller) {
            this.context = context;
            this.controller = controller;
        }

        @Override
        public void onReceive(@NonNull Context context, @NonNull Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                controller.getTransportControls().pause();
            }
        }

        public void register() {
            if (!registered) {
                context.registerReceiver(this, noisyFilter);
                registered = true;
            }
        }

        public void unregister() {
            if (registered) {
                context.unregisterReceiver(this);
                registered = false;
            }
        }
    }
}
