package com.liadpaz.music.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liadpaz.music.ActionReceiver;
import com.liadpaz.music.utils.Song;

import java.util.concurrent.TimeUnit;

public final class MusicPlayerService extends Service {

    @SuppressWarnings("unused")
    private static final String TAG = "MUSIC_SERVICE";

    private MusicPlayerBinder binder;

    public static MusicPlayerService instance;

    private OnSongStart onStartCallback;
    private OnSongPause onPauseCallback;
    private OnSongChange onChangeCallback;

    private MediaPlayer player;

    private Song source = null;

    private Song[] queue;
    private int currentSong = -1;

    public MusicPlayerService() {
        instance = this;
        player = new MediaPlayer();
        setListeners(null, null, null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new MusicPlayerBinder();
        getApplicationContext().registerReceiver(new ActionReceiver(), new IntentFilter());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void startQueue(@NonNull Song[] queue, int start) {
        this.queue = queue;
        this.currentSong = start;
        setAudio((source = queue[start]).getPath());
        start();
    }

    /**
     * This function sets the audio source
     *
     * @param pathToFile The file to play audio from
     */
    private void setAudio(String pathToFile) {
        try {
            player.reset();
            player.setDataSource(pathToFile);
            if (onChangeCallback != null) {
                onChangeCallback.onChange(source);
            }
            player.prepare();
        } catch (Exception ignored) {
        }
    }

    /**
     * This function starts the audio
     */
    private void start() {
        if (onStartCallback != null) {
            onStartCallback.onStart(source);
        }
        player.start();
    }

    public boolean isLooping() {
        return player.isLooping();
    }

    /**
     * This function determines if the player should loop or not
     *
     * @param looping {@code true} if the player should loop, otherwise {@code false}
     */
    public void setLooping(boolean looping) {
        player.setLooping(looping);
    }

    public Song getSource() {
        return source;
    }

    /**
     * This function returns the current millis of the played song
     *
     * @return current millis of the played song
     */
    private long getTime() {
        try {
            return player.getCurrentPosition();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void resetSong() {
        player.seekTo(0);
    }

    /**
     * This function stops the player from playing audio
     */
    private void pause() {
        player.pause();
        if (onPauseCallback != null) {
            onPauseCallback.onPause(source);
        }
    }

    /**
     * This function checks if music is playing
     *
     * @return true if the player plays music, otherwise false
     */
    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void playPrev() {
        if (getTime() < TimeUnit.SECONDS.toMillis(2)) {
            if (--currentSong == -1) {
                currentSong = queue.length - 1;
            }
            setAudio((source = queue[currentSong]).getPath());
            if (onChangeCallback != null) {
                onChangeCallback.onChange(queue[currentSong]);
            }
            start();
        } else {
            resetSong();
        }
    }

    public void startPause() {
        if (isPlaying()) {
            pause();
        } else {
            start();
        }
    }

    /**
     * This function plays the next song in queue, if the current song is the last in the queue then the first one will start
     */
    public void playNext() {
        if (++currentSong == queue.length) {
            currentSong = 0;
        }
        setAudio((source = queue[currentSong]).getPath());
        start();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasQueue() {
        return queue != null;
    }

    /**
     * This function sets the listeners for the service
     *
     * @param onStartCallback The on song start callback, if null will not set
     * @param onChangeCallback The on song change callback, if null will not set
     * @param onPauseCallback The on song pause callback, if null will not set
     */
    public void setListeners(@Nullable final OnSongStart onStartCallback, @Nullable final OnSongChange onChangeCallback, @Nullable final OnSongPause onPauseCallback) {
        if (onStartCallback != null) {
            this.onStartCallback = onStartCallback;
        }
        if (onPauseCallback != null) {
            this.onPauseCallback = onPauseCallback;
        }
        if (onChangeCallback != null) {
            this.onChangeCallback = onChangeCallback;
        }
        player.setOnCompletionListener(mp -> {
            if (!mp.isLooping()) {
                playNext();
                if (this.onChangeCallback != null) {
                    this.onChangeCallback.onChange(queue[currentSong]);
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public interface OnSongChange {
        void onChange(Song nextSong);
    }

    public interface OnSongPause {
        void onPause(Song song);
    }

    public interface OnSongStart {
        void onStart(Song song);
    }

    public class MusicPlayerBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }
}
