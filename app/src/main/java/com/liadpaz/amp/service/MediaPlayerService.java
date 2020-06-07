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
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.service.media.MediaBrowserService;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.notification.MediaNotification;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.Utilities;
import com.liadpaz.amp.viewmodels.Song;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public final class MediaPlayerService extends MediaBrowserService {
    private static final String TAG = "AmpApp.MediaPlayerService";

    private static final String LOG_TAG = "AmpApp.MEDIA_SESSION_LOG";
    private static final int NOTIFICATION_ID = 273;

    private boolean isLooping = false;

    private MediaPlayer mediaPlayer;
    private MediaSession mediaSession;
    private BecomingNoisyReceiver becomingNoisyReceiver;
    private MediaPlayer.OnCompletionListener onCompletionListener = mp -> {
        if (!isLooping) {
            mediaSession.getController().getTransportControls().skipToNext();
        } else {
            mediaSession.getController().getTransportControls().seekTo(0);
        }
    };

    private Executor executor;

    private MediaMetadata.Builder metadataBuilder;
    private PlaybackState.Builder playbackBuilder;

    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private AudioAttributes audioAttributes;
    private boolean resumeOnFocusGain = false;

    private Song currentSource;
    private int queuePosition = 0;
    private ArrayList<Song> queue = new ArrayList<>();

    private void setQueuePosition(int position) {
        QueueUtil.setPosition(position);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        executor = AsyncTask.SERIAL_EXECUTOR;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(onCompletionListener);

        new MediaSession.QueueItem(new MediaDescription.Builder().build(), 0);

        QueueUtil.observeQueue(songs -> {
            this.queue = songs;
            if (this.queue.size() > 0 && currentSource == null) {
                new LoadSongTask(this).executeOnExecutor(executor);
            }
        });
        QueueUtil.observePosition(queuePosition -> {
            if (queuePosition != -1) {
                this.queuePosition = queuePosition;
                if (!QueueUtil.isChanging) {
                    new LoadSongTask(this).executeOnExecutor(executor);
                    new PlaySongTask(this).executeOnExecutor(executor);
                } else {
                    QueueUtil.isChanging = false;
                }
            }
        });

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build();

        mediaPlayer.setAudioAttributes(audioAttributes);

        mediaSession = new MediaSession(this, LOG_TAG);
        mediaSession.setSessionActivity(PendingIntent.getActivity(this, 10, new Intent(this, MainActivity.class).setAction(Constants.PREFERENCES_SHOW_CURRENT), 0));

        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onCommand(@NonNull String command, Bundle extras, ResultReceiver cb) {
                if (Constants.LOOP_EXTRA.equals(command)) {
                    setLooping(extras.getBoolean(Constants.LOOP_EXTRA));
                    sendPlaybackState(mediaPlayer.getCurrentPosition(), mediaPlayer.isPlaying() ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED);
                }
            }

            @Override
            public void onPlay() {
                Log.d(TAG, "onPlay() called");
                new PlaySongTask(MediaPlayerService.this).executeOnExecutor(executor);
            }

            @Override
            public void onPause() {
                MediaPlayerService.this.onPause();
                resumeOnFocusGain = false;
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            }

            @Override
            public void onStop() {
                resumeOnFocusGain = false;
                mediaPlayer.stop();
                mediaSession.setActive(false);
                if (audioFocusRequest != null) {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest);
                }
                currentSource = null;
                sendPlaybackState(mediaPlayer.getCurrentPosition(), PlaybackState.STATE_STOPPED);
                sendMetadata(null);
                stopForeground(true);
            }

            @SuppressWarnings("ConstantConditions")
            @Override
            public void onSkipToNext() {
                if (mediaSession.getController().getPlaybackState().getState() != PlaybackState.STATE_NONE) {
                    if (queue.size() > 0) {
                        if (queuePosition == queue.size() - 1) {
                            setQueuePosition(0);
                        } else {
                            setQueuePosition(queuePosition + 1);
                        }
                    }
                }
            }

            @SuppressWarnings("ConstantConditions")
            @Override
            public void onSkipToPrevious() {
                if (mediaSession.getController().getPlaybackState().getState() != PlaybackState.STATE_NONE) {
                    if (TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()) > 2) {
                        onSeekTo(0);
                    } else if (queue.size() > 0) {
                        if (queuePosition == 0) {
                            setQueuePosition(queue.size() - 1);
                        } else {
                            setQueuePosition(queuePosition - 1);
                        }
                    }
                }
            }

            @SuppressWarnings("ConstantConditions")
            @Override
            public void onSeekTo(long pos) {
                if (mediaSession.getController().getPlaybackState().getState() != PlaybackState.STATE_NONE) {
                    mediaPlayer.seekTo((int)pos);
                    if (resumeOnFocusGain) {
                        mediaPlayer.start();
                    }
                    sendPlaybackState(mediaPlayer.getCurrentPosition(), mediaPlayer.isPlaying() ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED);
                }
            }
        });

        metadataBuilder = new MediaMetadata.Builder();
        mediaSession.setMetadata(metadataBuilder.build());

        playbackBuilder = new PlaybackState.Builder();
        sendPlaybackState(mediaPlayer.getCurrentPosition(), PlaybackState.STATE_STOPPED);

        setSessionToken(mediaSession.getSessionToken());

        becomingNoisyReceiver = new BecomingNoisyReceiver(this, mediaSession.getController());
    }

    private void onPlay() {
        Log.d(TAG, "onPlay: ");
        audioManager.requestAudioFocus(audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setOnAudioFocusChangeListener(focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN: {
                    if (resumeOnFocusGain) {
                        resumeOnFocusGain = false;
                        mediaPlayer.setVolume(1F, 1F);
                        play();
                    }
                    break;
                }

                case AudioManager.AUDIOFOCUS_LOSS: {
                    resumeOnFocusGain = false;
                    onPause();
                    break;
                }

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                    mediaPlayer.setVolume(0.2F, 0.2F);
                    break;
                }

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                    resumeOnFocusGain = mediaPlayer.isPlaying();
                    onPause();
                    break;
                }
            }
        }).setAudioAttributes(audioAttributes).build());
        mediaPlayer.setVolume(1F, 1F);
        play();
    }

    private void play() {
        resumeOnFocusGain = true;
        becomingNoisyReceiver.register();
        mediaPlayer.start();
        sendPlaybackState(mediaPlayer.getCurrentPosition(), PlaybackState.STATE_PLAYING);
        startNotification();
    }

    private void onPause() {
        becomingNoisyReceiver.unregister();
        mediaPlayer.pause();
        sendPlaybackState(mediaPlayer.getCurrentPosition(), PlaybackState.STATE_PAUSED);
        startNotification();
    }

    private void setLooping(boolean isLooping) {
        this.isLooping = isLooping;
        sendPlaybackState(mediaPlayer.getCurrentPosition(), mediaPlayer.isPlaying() ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED);
        startNotification();
    }

    private void sendMetadata(@Nullable Song song) {
        if (song != null) {
            metadataBuilder.putText(MediaMetadata.METADATA_KEY_TITLE, song.songTitle).putText(MediaMetadata.METADATA_KEY_ARTIST, Utilities.joinArtists(song.songArtists)).putText(MediaMetadata.METADATA_KEY_ALBUM, song.album).putString(MediaMetadata.METADATA_KEY_ART_URI, Utilities.getCoverUri(song).toString()).putLong(MediaMetadata.METADATA_KEY_DURATION, mediaPlayer.getDuration());
            mediaSession.setMetadata(metadataBuilder.build());
        } else {
            mediaSession.setMetadata(null);
        }
    }

    private void sendPlaybackState(int position, int state) {
        playbackBuilder.setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_SEEK_TO | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS).setState(state, position, 1F);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.LOOP_EXTRA, isLooping);
        playbackBuilder.setExtras(bundle);
        mediaSession.setPlaybackState(playbackBuilder.build());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.hasExtra(Constants.ACTION)) {
                switch (intent.getStringExtra(Constants.ACTION)) {
                    case Constants.ACTION_SKIP_PREV: {
                        mediaSession.getController().getTransportControls().skipToPrevious();
                        break;
                    }

                    case Constants.ACTION_PAUSE: {
                        mediaSession.getController().getTransportControls().pause();
                        break;
                    }

                    case Constants.ACTION_PLAY: {
                        mediaSession.getController().getTransportControls().play();
                        break;
                    }

                    case Constants.ACTION_SKIP_NEXT: {
                        mediaSession.getController().getTransportControls().skipToNext();
                        break;
                    }
                }
            } else if (intent.hasExtra(Constants.LOOP_EXTRA)) {
                setLooping(intent.getExtras().getBoolean(Constants.LOOP_EXTRA, false));
            }
        }
        return START_STICKY;
    }

    private void setSource(Song song) {
        try {
            if (audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            }
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(this, ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, (currentSource = song).songId));
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            sendMetadata(currentSource);
        } catch (Exception ignored) {
        }
    }

    private void startNotification() {
        final boolean isPlaying = mediaPlayer.isPlaying();

        try {
            final Notification.Builder builder = MediaNotification.from(this, mediaSession);

            if (builder != null) {
                Glide.with(getApplicationContext()).asBitmap().load(Utilities.getCoverUri(currentSource)).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap cover, @Nullable Transition<? super Bitmap> transition) {
                        startForeground(NOTIFICATION_ID, builder.setLargeIcon(cover).build());
                        if (!isPlaying) {
                            stopForeground(false);
                        }
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Bitmap cover = Utilities.getBitmapFromVectorDrawable(MediaPlayerService.this, R.drawable.song);

                        startForeground(NOTIFICATION_ID, builder.setLargeIcon(cover).build());
                        if (!isPlaying) {
                            stopForeground(false);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
            }
        } catch (Exception ignored) {
        }
    }

    @NonNull
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) { return new BrowserRoot(getString(R.string.app_name), null); }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowser.MediaItem>> result) {
        ArrayList<MediaBrowser.MediaItem> mediaItems = new ArrayList<>();
        for (Song song : queue) {
            MediaDescription.Builder descriptionBuilder = new MediaDescription.Builder().setTitle(song.songTitle).setSubtitle(Utilities.joinArtists(song.songArtists)).setDescription(song.album).setIconUri(Utilities.getCoverUri(song));
            mediaItems.add(new MediaBrowser.MediaItem(descriptionBuilder.build(), MediaBrowser.MediaItem.FLAG_PLAYABLE));
        }
        result.sendResult(mediaItems);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        becomingNoisyReceiver.unregister();
        mediaPlayer.release();
        mediaPlayer = null;
        mediaSession.release();
    }

    private static class BecomingNoisyReceiver extends BroadcastReceiver {
        private IntentFilter noisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        private Context context;
        private MediaController controller;

        private boolean registered = false;

        public BecomingNoisyReceiver(@NonNull Context context, @NonNull MediaController controller) {
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

    private static class LoadSongTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<MediaPlayerService> service;

        LoadSongTask(@NonNull MediaPlayerService service) {
            this.service = new WeakReference<>(service);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                service.get().setSource(service.get().queue.get(service.get().queuePosition));
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    private static class PlaySongTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<MediaPlayerService> service;

        PlaySongTask(@NonNull MediaPlayerService service) {
            this.service = new WeakReference<>(service);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            service.get().onPlay();
            return null;
        }
    }
}
