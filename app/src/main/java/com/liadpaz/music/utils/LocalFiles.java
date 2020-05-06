package com.liadpaz.music.utils;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class LocalFiles {

    private static SharedPreferences musicSharedPreferences;

    public LocalFiles(SharedPreferences musicSharedPreferences) {
        LocalFiles.musicSharedPreferences = musicSharedPreferences;
    }

    /**
     * This function returns the music folder path, if not exists returns the default one
     *
     * @return The music folder path, if not exists returns the default one
     */
    @NonNull
    public static String getPath() {
        return musicSharedPreferences.getString(Constants.SHARED_PREFERENCES_PATH, Constants.DEFAULT_PATH);
    }

    /**
     * This function saves the path of the songs folder to the local shared preferences
     *
     * @param path The path of the songs folder
     */
    public static void setPath(@NonNull String path) {
        musicSharedPreferences.edit().putString(Constants.SHARED_PREFERENCES_PATH, path).apply();
    }

    /**
     * This function returns the ArrayList of the songs
     *
     * @return The ArrayList of the songs
     */
    @Nullable
    public static ArrayList<Song> getSongs() {
        return new Gson().fromJson(musicSharedPreferences.getString(Constants.SHARED_PREFERENCES_SONGS, null), new TypeToken<ArrayList<Song>>() {}.getType());
    }

    /**
     * This function saves the songs list to the local shared preferences
     *
     * @param songs ArrayList of the songs
     */
    public static void setSongs(@NonNull List<Song> songs) {
        musicSharedPreferences.edit().putString(Constants.SHARED_PREFERENCES_SONGS, new Gson().toJson(songs)).apply();
    }
}
