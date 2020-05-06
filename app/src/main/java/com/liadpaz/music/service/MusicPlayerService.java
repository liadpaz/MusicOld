package com.liadpaz.music.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liadpaz.music.utils.Song;

import java.util.concurrent.TimeUnit;

public final class MusicPlayerService extends Service {

    private static final String TAG = "MUSIC_SERVICE";

    private MusicPlayerBinder binder;

    private OnCompleteListener callback;

    private MediaPlayer player;

    private Song source = null;

    private Song[] queue;
    private int currentSong = -1;

    public MusicPlayerService() {
        player = new MediaPlayer();
        setOnNextSongListener(null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new MusicPlayerBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }



    public void startQueue(@NonNull Song[] queue, int start) {
        Log.d(TAG, "startQueue: " + start);
        this.queue = queue;
        this.currentSong = start;
        setAudio((source = queue[start]).getPath());
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
            player.prepare();
        } catch (Exception ignored) {
        }
    }

    /**
     * This function starts the audio
     */
    public void start() {
        player.start();
    }

    /**
     * This function determines if the player should loop or not
     *
     * @param looping {@code true} if the player should loop, otherwise {@code false}
     */
    public void setLooping(boolean looping) {
        player.setLooping(looping);
    }

    public boolean isLooping() {
        return player.isLooping();
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
    public void pause() {
        player.pause();
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
            if (callback != null) {
                callback.onComplete(queue[currentSong]);
            }
            start();
        } else {
            resetSong();
        }
    }

    public void playNext() {
        if (++currentSong == queue.length) {
            currentSong = 0;
        }
        callback.onComplete(queue[currentSong]);
        setAudio((source = queue[currentSong]).getPath());
        start();
    }

    public Song getCurrentSong() {
        return queue[currentSong];
    }

    public boolean hasQueue() {
        return queue != null;
    }

    public void setOnNextSongListener(final OnCompleteListener callback) {
        this.callback = callback;
        player.setOnCompletionListener(mp -> {
            if (!mp.isLooping()) {
                playNext();
                if (callback != null) {
                    callback.onComplete(queue[currentSong]);
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public interface OnCompleteListener {
        void onComplete(Song nextSong);
    }

    public class MusicPlayerBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }
}
