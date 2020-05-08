package com.liadpaz.music.utils;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalFiles {

    private static final String TAG = "LOCAL_FILES";
    private static SharedPreferences musicSharedPreferences;
    private static File songsFile;

    public LocalFiles(SharedPreferences musicSharedPreferences, File songsFile) {
        LocalFiles.musicSharedPreferences = musicSharedPreferences;
        LocalFiles.songsFile = songsFile;
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
        musicSharedPreferences.edit()
                              .putString(Constants.SHARED_PREFERENCES_PATH, path)
                              .apply();
    }

    /**
     * This function returns the ArrayList of the songs
     *
     * @return The ArrayList of the songs
     */
    @Nullable
    public static ArrayList<String> getSongs() {
        return new Gson().fromJson(musicSharedPreferences.getString(Constants.SHARED_PREFERENCES_SONGS, null), new TypeToken<ArrayList<String>>() {}.getType());
    }

    /**
     * This function saves the songs list to the local shared preferences
     *
     * @param songs ArrayList of the songs
     */
    public static void setSongs(@NonNull List<String> songs) {
        musicSharedPreferences.edit()
                              .putString(Constants.SHARED_PREFERENCES_SONGS, new Gson().toJson(songs))
                              .apply();
    }

    public static void resetSongs() {
        musicSharedPreferences.edit()
                              .remove(Constants.SHARED_PREFERENCES_SONGS)
                              .apply();
    }
}
