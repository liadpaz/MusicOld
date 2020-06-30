package com.liadpaz.amp.service;

import android.content.ComponentName;
import android.content.Context;
import android.media.MediaMetadata;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.android.exoplayer2.source.ConcatenatingMediaSource;

import java.util.function.BiConsumer;

public class ServiceConnector {
    private static final String TAG = "AmpApp.ServiceConnector";

    private volatile static ServiceConnector instance;

    private final PlaybackStateCompat EMPTY_PLAYBACK_STATE = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_NONE, 0, 0f).build();
    private final MediaMetadataCompat NOTHING_PLAYING = new MediaMetadataCompat.Builder().putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "").putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0).build();

    public final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);
    public final MutableLiveData<PlaybackStateCompat> playbackState = new MutableLiveData<>(EMPTY_PLAYBACK_STATE);
    public final MutableLiveData<MediaMetadataCompat> nowPlaying = new MutableLiveData<>(NOTHING_PLAYING);
    public final MutableLiveData<Integer> repeatMode = new MutableLiveData<>(PlaybackStateCompat.REPEAT_MODE_ALL);
    public final MutableLiveData<ConcatenatingMediaSource> mediaSource = new MutableLiveData<>(new ConcatenatingMediaSource());

    public MediaControllerCompat.TransportControls transportControls;
    private MediaBrowserConnectionCallback mediaBrowserConnectionCallback;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;

    private ServiceConnector(@NonNull Context context, @NonNull ComponentName componentName) {
        mediaBrowser = new MediaBrowserCompat(context, componentName, mediaBrowserConnectionCallback = new MediaBrowserConnectionCallback(context), null);
        mediaBrowser.connect();
    }

    public static ServiceConnector getInstance(@NonNull Context context, @NonNull ComponentName componentName) {
        if (instance == null) {
            instance = new ServiceConnector(context, componentName);
        }
        return instance;
    }

    public static ServiceConnector getInstance() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    public void subscribe(@NonNull String parentId, @NonNull MediaBrowserCompat.SubscriptionCallback subscriptionCallback) {
        mediaBrowser.subscribe(parentId, subscriptionCallback);
    }

    public void unsubscribe(@NonNull String parentId, @NonNull MediaBrowserCompat.SubscriptionCallback subscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, subscriptionCallback);
    }

    public void sendCommand(@NonNull String command, @Nullable Bundle parameters) {
        sendCommand(command, parameters, (ignored1, ignored2) -> {});
    }

    public void sendCommand(@NonNull String command, @Nullable Bundle parameters, @NonNull BiConsumer<Integer, Bundle> resultCallback) {
        mediaController.sendCommand(command, parameters, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, @Nullable Bundle resultData) {
                resultCallback.accept(resultCode, resultData);
            }
        });
    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        private static final String TAG = "AmpApp.MediaBrowserConnectionC";

        private Context context;

        MediaBrowserConnectionCallback(@NonNull Context context) {
            this.context = context;
        }

        @Override
        public void onConnected() {
            try {
                mediaController = new MediaControllerCompat(context, mediaBrowser.getSessionToken());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mediaController.registerCallback(new MediaControllerCallback());
            transportControls = mediaController.getTransportControls();

            isConnected.postValue(true);
        }

        @Override
        public void onConnectionSuspended() {
            isConnected.postValue(false);
        }

        @Override
        public void onConnectionFailed() {
            isConnected.postValue(false);
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        private static final String TAG = "AmpApp.MediaControllerCallback";

        @Override
        public void onPlaybackStateChanged(@Nullable PlaybackStateCompat state) { playbackState.postValue(state); }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onMetadataChanged(@Nullable MediaMetadataCompat metadata) { nowPlaying.postValue(metadata.getString(MediaMetadata.METADATA_KEY_MEDIA_ID) == null ? NOTHING_PLAYING : metadata); }

        @Override
        public void onRepeatModeChanged(int repeatMode) { ServiceConnector.this.repeatMode.postValue(mediaController.getRepeatMode()); }

        @Override
        public void onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended();
        }
    }

    public static void playFromQueue() {
        getInstance().transportControls.playFromMediaId("queue", null);
    }
}
