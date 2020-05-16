package com.liadpaz.amp.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.liadpaz.amp.MainActivity;
import com.liadpaz.amp.R;
import com.liadpaz.amp.notification.MediaNotification;
import com.liadpaz.amp.utils.Constants;
import com.liadpaz.amp.utils.LocalFiles;
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

    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;

    private MediaMetadataCompat.Builder metadataBuilder;
    private PlaybackStateCompat.Builder playbackBuilder;

    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    private boolean shouldPlay = false;

    private Song currentSource;
    private ArrayList<Song> queue = new ArrayList<>();
    private int queuePosition;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            if (mediaPlayer.isLooping()) {
                mediaSession.getController().getTransportControls().seekTo(0);
            } else {
                mediaSession.getController().getTransportControls().skipToNext();
            }
        });

        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);

        mediaSession = new MediaSessionCompat(this, LOG_TAG);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setSessionActivity(PendingIntent.getActivity(this, 10, new Intent(this, MainActivity.class), 0));

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onCommand(String command, Bundle extras, ResultReceiver cb) {
                switch (command) {
                    case Constants.ACTION_SET_QUEUE: {
                        queue = LocalFiles.getQueue();
                        int pos = 0;
                        if (extras != null) {
                            pos = extras.getInt(Constants.ACTION_QUEUE_POSITION);
                        }
                        setSource(queue.get(queuePosition = pos));
                        onPlay();
                        break;
                    }

                    case Constants.ACTION_QUEUE_POSITION: {
                        int pos = 0;
                        if (extras != null) {
                            pos = extras.getInt(Constants.ACTION_QUEUE_POSITION);
                        }
                        setSource(queue.get(queuePosition = pos));
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
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                            if (shouldPlay) {
                                mediaSession.getController().getTransportControls().pause();
                                shouldPlay = true;
                            } else {
                                mediaSession.getController().getTransportControls().pause();
                            }
                            break;
                        }

                        case AudioManager.AUDIOFOCUS_LOSS: {
                            mediaSession.getController().getTransportControls().stop();
                            break;
                        }

                        case AudioManager.AUDIOFOCUS_GAIN: {
                            mediaPlayer.setVolume(1F, 1F);
                            if (shouldPlay) {
                                onPlay();
                            }
                            break;
                        }

                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK: {
                            mediaPlayer.setVolume(0.5F, 0.5F);
                            break;
                        }
                    }
                }).build()) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    shouldPlay = true;
                    mediaPlayer.start();
                    sendPlaybackState();
                    startNotification(true);
                }
            }

            @Override
            public void onPause() {
                shouldPlay = false;
                mediaPlayer.pause();
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
                sendPlaybackState();
                startNotification(false);
            }

            @Override
            public void onStop() {
                shouldPlay = false;
                mediaPlayer.stop();
                mediaSession.setActive(false);
                sendPlaybackState();
                stopSelf();
            }

            @Override
            public void onSkipToNext() {
                if (queue.size() > 0) {
                    if (++queuePosition == queue.size()) {
                        queuePosition = 0;
                    }
                    setSource(queue.get(queuePosition));
                    sendMetadata(currentSource);
                    onPlay();
                }
            }

            @Override
            public void onSkipToPrevious() {
                if (TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()) > 2) {
                    onSeekTo(0);
                } else if (queue.size() > 0) {
                    if (--queuePosition == -1) {
                        queuePosition = queue.size() - 1;
                    }
                    setSource(queue.get(queuePosition));
                    sendMetadata(currentSource);
                    onPlay();
                }
            }

            @Override
            public void onSeekTo(long pos) {
                mediaPlayer.seekTo((int)pos);
                sendPlaybackState();
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
    }

    private void sendMetadata(@NonNull Song song) {
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_TITLE, song.getSongTitle()).putText(MediaMetadataCompat.METADATA_KEY_ARTIST, Utilities.joinArtists(song.getSongArtists())).putText(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbum()).putString(MediaMetadataCompat.METADATA_KEY_ART_URI, Utilities.getCover(song).toString()).putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration());
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
        try {
            Bitmap cover = BitmapFactory.decodeStream(getContentResolver().openInputStream(Utilities.getCover(currentSource)));
            startForeground(NOTIFICATION_ID, builder.setLargeIcon(cover).build());
        } catch (Exception ignored) {
            Bitmap placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.song);
            startForeground(NOTIFICATION_ID, builder.setLargeIcon(placeholder).setColor(getColor(R.color.colorPrimary)).build());
            if (!isPlaying) {
                stopForeground(false);
            }
        }
        if (!isPlaying) {
            stopForeground(false);
        }
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
        stopForeground(true);
    }
}
