package com.liadpaz.music.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

public final class MusicPlayerService extends Service {

    MediaPlayer player;

    public MusicPlayerService() {
        super();

        player = new MediaPlayer();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }

    /**
     * This function sets the audio source
     *
     * @param pathToFile The file to play audio from
     */
    public void setAudio(String pathToFile) {
        try {
            player.setDataSource(pathToFile);
        } catch (Exception e) {
            e.printStackTrace();
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

    /**
     * This function stops the player from playing audio
     */
    public void stop() {
        player.start();
    }

    @Nullable
    @Override
    public IBinder onBind(@Nullable Intent intent) {
        return null;
    }
}
