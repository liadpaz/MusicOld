package com.liadpaz.amp.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.livedatautils.SongsUtil;
import com.liadpaz.amp.notification.AmpNotificationManager;
import com.liadpaz.amp.utils.AmpPlaybackPreparer;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MediaPlayerService extends MediaBrowserServiceCompat {
    private static final String TAG = "AmpApp.MediaPlayerService";
    private static final String LOG_TAG = "AmpApp2.MediaSessionLog";

    private BecomingNoisyReceiver becomingNoisyReceiver;
    private AmpNotificationManager notificationManager;

    private MediaSessionCompat mediaSession;

    private boolean isForeground = false;

    private AudioAttributes ampAudioAttributes = new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build();

    private final PlayerEventListener playerEventListener = new PlayerEventListener();

    private SimpleExoPlayer exoPlayer;

    // observers
    //    private Observer<Integer> observerPosition;
    //    private Observer<ArrayList<Song>> observerQueue;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate() {
        super.onCreate();

        final PendingIntent sessionActivityPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), 0);

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this,
                new DefaultTrackSelector(),
                new DefaultLoadControl.Builder()
                        .setAllocator(new DefaultAllocator(true, 64))
                        .setTargetBufferBytes(-1)
                        .createDefaultLoadControl());
        exoPlayer.setAudioAttributes(ampAudioAttributes, true);
        exoPlayer.addListener(playerEventListener);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

        mediaSession = new MediaSessionCompat(this, LOG_TAG) {{
            setSessionActivity(sessionActivityPendingIntent);
            setActive(true);
        }};

        becomingNoisyReceiver = new BecomingNoisyReceiver(getApplicationContext(), mediaSession.getController());

        setSessionToken(mediaSession.getSessionToken());

        notificationManager = new AmpNotificationManager(this, exoPlayer, getSessionToken(), new PlayerNotificationListener());

        MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)));
        MediaSessionConnector.PlaybackPreparer playbackPreparer = new AmpPlaybackPreparer(exoPlayer, dataSourceFactory);

        mediaSessionConnector.setPlayer(exoPlayer);
        mediaSessionConnector.setPlaybackPreparer(playbackPreparer);
        mediaSessionConnector.setQueueNavigator(new AmpQueueNavigator(mediaSession));
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        exoPlayer.stop(true);
    }

    @Override
    public void onDestroy() {
        mediaSession.setActive(false);
        mediaSession.release();

        exoPlayer.removeListener(playerEventListener);
        exoPlayer.release();
    }

    @NonNull
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) { return new BrowserRoot(getString(R.string.app_name), null); }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for (Song song : SongsUtil.getSongs()) {
            mediaItems.add(song.toMediaItem());
        }
        result.sendResult(mediaItems);
    }

    @Override
    public void onSearch(@NonNull String query, Bundle extras, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        String finalQuery = query.toLowerCase();
        List<MediaBrowserCompat.MediaItem> queriedSongs = SongsUtil.getSongs().stream().filter(song -> song.isMatchingQuery(finalQuery)).map(Song::toMediaItem).collect(Collectors.toCollection(ArrayList::new));
        if (queriedSongs.size() == 0) {
            queriedSongs.add(null);
        }
        result.sendResult(queriedSongs);
    }

    private class PlayerNotificationListener implements PlayerNotificationManager.NotificationListener {
        private static final String TAG = "AmpApp.PlayerNotificationListener";

        @Override
        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            if (ongoing && !isForeground) {
                ContextCompat.startForegroundService(getApplicationContext(), new Intent(getApplication(), MediaPlayerService.class));
                startForeground(notificationId, notification);
                isForeground = true;
            }
        }

        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            stopForeground(true);
            isForeground = false;
            stopSelf();
        }
    }

    private class PlayerEventListener implements Player.EventListener {
        private static final String TAG = "AmpApp.PlayerEventListener";

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING) {
                notificationManager.showNotification();
                becomingNoisyReceiver.register();
                if (playbackState == Player.STATE_READY) {
                    if (!playWhenReady) {
                        stopForeground(false);
                    }
                }
            } else {
                notificationManager.hideNotification();
                becomingNoisyReceiver.unregister();
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            QueueUtil.setPosition(exoPlayer.getCurrentWindowIndex());
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onTimelineChanged(@NonNull Timeline timeline, @Nullable Object manifest, int reason) {
            Log.d(TAG, "onTimelineChanged reason: " + reason);
            if (reason == Player.TIMELINE_CHANGE_REASON_PREPARED) {
                List<Song> queue = new ArrayList<>();
                Timeline.Window window = new Timeline.Window();
                for (int i = 0; i < timeline.getWindowCount(); i++) {
                    queue.add(new Song((MediaDescriptionCompat)timeline.getWindow(i, window, true).tag));
                }
                QueueUtil.setQueue(queue);
            }
            QueueUtil.setPosition(exoPlayer.getCurrentWindowIndex());
        }
    }

    private static class AmpQueueNavigator extends TimelineQueueNavigator {
        private static final String TAG = "AmpApp.AmpQueueNavigator";

        private final Timeline.Window window = new Timeline.Window();

        public AmpQueueNavigator(@NonNull MediaSessionCompat mediaSession) {
            super(mediaSession);
        }

        @Override
        public MediaDescriptionCompat getMediaDescription(@NonNull Player player, int windowIndex) {
            return (MediaDescriptionCompat)player.getCurrentTimeline().getWindow(windowIndex, window, true).tag;
        }
    }

    /**
     * This class is for the <i>Becoming Noisy</i> broadcast, eg. when the user hears music with
     * earphones and the earphones disconnects.
     * <p>
     * It's stopping the playback when it receives that broadcast.
     */
    private static class BecomingNoisyReceiver extends BroadcastReceiver {
        private static final String TAG = "AmpApp.BecomingNoisyReceiver";

        private final Context context;

        private final IntentFilter noisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        private final MediaControllerCompat controller;

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
