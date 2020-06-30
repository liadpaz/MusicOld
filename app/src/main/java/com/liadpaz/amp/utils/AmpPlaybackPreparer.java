package com.liadpaz.amp.utils;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.service.ServiceConnector;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;

public class AmpPlaybackPreparer implements MediaSessionConnector.PlaybackPreparer {
    private static final String TAG = "AmpApp.PlaybackPreparer";

    private final ExoPlayer exoPlayer;
    private final DataSource.Factory dataSourceFactory;

    private static DataSource.Factory staticDataSourceFactory;

    public AmpPlaybackPreparer(@NonNull ExoPlayer exoPlayer, @NonNull DataSource.Factory dataSourceFactory) {
        this.exoPlayer = exoPlayer;
        this.dataSourceFactory = dataSourceFactory;
        staticDataSourceFactory = dataSourceFactory;
    }

    @Override
    public long getSupportedPrepareActions() {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
    }

    @Override
    public void onPrepare() {}

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onPrepareFromMediaId(@NonNull String mediaId, @Nullable Bundle extras) {
        ConcatenatingMediaSource mediaSource = new ConcatenatingMediaSource(true);
        for (Song song : QueueUtil.getQueue()) {
            mediaSource.addMediaSource(new ProgressiveMediaSource.Factory(dataSourceFactory).setTag(song.mediaMetadata.getDescription()).createMediaSource(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)));
        }
        ServiceConnector.getInstance().mediaSource.setValue(mediaSource);
        exoPlayer.prepare(mediaSource);
        exoPlayer.seekTo(QueueUtil.getPosition(), 0);
    }

    @Override
    public void onPrepareFromSearch(@NonNull String query, @Nullable Bundle extras) {
    }

    @Override
    public void onPrepareFromUri(Uri uri, Bundle extras) {
    }

    @Override
    public boolean onCommand(Player player, ControlDispatcher controlDispatcher, String command, Bundle extras, ResultReceiver cb) {
        return false;
    }

    @NonNull
    private static MediaSource createMediaSource(@NonNull Song song) {
        return new ProgressiveMediaSource.Factory(staticDataSourceFactory).setTag(song.mediaMetadata.getDescription()).createMediaSource(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id));
    }

    @SuppressWarnings("ConstantConditions")
    public static void addToQueue(@NonNull Song song) {
        ServiceConnector.getInstance().mediaSource.getValue().addMediaSource(createMediaSource(song));
        ArrayList<Song> songs = QueueUtil.getQueue();
        songs.add(song);
        QueueUtil.setQueue(songs);
    }

    @SuppressWarnings("ConstantConditions")
    public static void addToQueueNext(@NonNull Song song) {
        ServiceConnector.getInstance().mediaSource.getValue().addMediaSource(QueueUtil.getPosition() + 1, createMediaSource(song));
        ArrayList<Song> songs = QueueUtil.getQueue();
        songs.add(QueueUtil.getPosition() + 1, song);
        QueueUtil.setQueue(songs);
    }
}
