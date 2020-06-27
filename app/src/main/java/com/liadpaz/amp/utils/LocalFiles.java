package com.liadpaz.amp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liadpaz.amp.livedatautils.PlaylistsUtil;
import com.liadpaz.amp.livedatautils.QueueUtil;
import com.liadpaz.amp.livedatautils.SongsUtil;
import com.liadpaz.amp.viewmodels.Playlist;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class LocalFiles {
    private static final String TAG = "AmpApp.LocalFiles";

    private static final String[] PROJECTION = {Media.TITLE, Media._ID, Media.ARTIST, Media.ALBUM, Media.ALBUM_ID};

    private static SharedPreferences musicSharedPreferences;
    private static SharedPreferences playlistsSharedPreferences;

    private static String SORT_DEFAULT = Media.TITLE + " COLLATE NOCASE";

    private static AtomicBoolean isFirstTimeQueue = new AtomicBoolean(true);
    private static AtomicBoolean isFirstTimePosition = new AtomicBoolean(true);

    public static void init(@NonNull Context context) {
        musicSharedPreferences = context.getSharedPreferences("Music.Data", 0);
        playlistsSharedPreferences = context.getSharedPreferences("Music.Playlists", 0);

        ContentResolver contentResolver = context.getContentResolver();

        CompletableFuture.runAsync(() -> PlaylistsUtil.setPlaylists(getPlaylists(contentResolver)));
        CompletableFuture.runAsync(() -> SongsUtil.setSongs(listSongs(contentResolver, SORT_DEFAULT)));

        QueueUtil.observeQueue(songs -> {
            if (!isFirstTimeQueue.getAndSet(false)) {
                ArrayList<Long> songsIdsList = songs.stream().map(song -> song.songId).collect(Collectors.toCollection(ArrayList::new));
                musicSharedPreferences.edit().putString(Constants.PREFERENCES_QUEUE, new Gson().toJson(songsIdsList)).apply();
            }
        });
        QueueUtil.observePosition(queuePosition -> {
            if (!isFirstTimePosition.getAndSet(false)) {
                musicSharedPreferences.edit().putInt(Constants.PREFERENCES_QUEUE_POSITION, queuePosition).apply();
            }
        });

        //        loadQueue(context.getContentResolver());
        // TODO: fix queue saving
    }

    private static void loadQueue(@NonNull ContentResolver contentResolver) {
        List<Long> songsIds = new Gson().fromJson(musicSharedPreferences.getString(Constants.PREFERENCES_QUEUE, "[]"), new TypeToken<ArrayList<Long>>() {}.getType());
        List<Song> songs = listSongs(contentResolver, SORT_DEFAULT, songsIds);
        Collections.sort(songs, Comparator.comparing(song -> songsIds.indexOf(song.songId)));
        QueueUtil.setQueue(songs);
        QueueUtil.setPosition(musicSharedPreferences.getInt(Constants.PREFERENCES_QUEUE_POSITION, -1));
    }

    /**
     * This function returns the music folder path, if it does not exists, it returns the default
     * one.
     *
     * @return The music folder path, if not exists returns the default one.
     */
    @NonNull
    public static String getPath() {
        return musicSharedPreferences.getString(Constants.PREFERENCES_PATH, Constants.DEFAULT_PATH);
    }

    /**
     * This function saves the path of the songs folder to the local shared preferences.
     *
     * @param path The path of the songs folder.
     */
    public static void setPath(@NonNull final String path) {
        musicSharedPreferences.edit().putString(Constants.PREFERENCES_PATH, path).apply();
    }

    /**
     * This function returns the show current song screen on notification click flag.
     *
     * @return true to show current song screen on notification click, otherwise false.
     */
    public static boolean getShowCurrent() {
        return musicSharedPreferences.getBoolean(Constants.PREFERENCES_SHOW_CURRENT, true);
    }

    /**
     * This function sets the show current song screen on notification click flag.
     *
     * @param showCurrent true to show current song screen on notification click flag, otherwise
     *                    false.
     */
    public static void setShowCurrent(final boolean showCurrent) {
        musicSharedPreferences.edit().putBoolean(Constants.PREFERENCES_SHOW_CURRENT, showCurrent).apply();
    }

    /**
     * This function returns the keep screen on when the current song screen is shown flag.
     *
     * @return true to keep screen on when the current song screen is shown, otherwise false.
     */
    public static boolean getScreenOn() {
        return musicSharedPreferences.getBoolean(Constants.PREFERENCES_SCREEN_ON, false);
    }

    /**
     * This function sets the keep screen on when the current song screen is shown flag.
     *
     * @param screenOn true to keep screen on when the current song screen is shown, otherwise
     *                 false.
     */
    public static void setScreenOn(final boolean screenOn) {
        musicSharedPreferences.edit().putBoolean(Constants.PREFERENCES_SCREEN_ON, screenOn).apply();
    }

    @NonNull
    private static ArrayList<Song> listSongs(@NonNull ContentResolver contentResolver, @NonNull String sort) {
        return listSongs(contentResolver, sort, null);
    }

    @NonNull
    private static ArrayList<Song> listSongs(@NonNull ContentResolver contentResolver, @NonNull String sort, @Nullable List<Long> songsIds) {
        long start = System.currentTimeMillis();
        ArrayList<Song> songs = new ArrayList<>();
        //retrieve song info
        try (Cursor musicCursor = contentResolver.query(Media.EXTERNAL_CONTENT_URI, PROJECTION,  Media.DATA + " like ? ", new String[]{"%" + getPath() + "%"}, sort)) {
            //iterate over results if valid
            if (musicCursor != null && musicCursor.moveToFirst()) {
                //get columns
                int titleColumn = musicCursor.getColumnIndex(Media.TITLE);
                int idColumn = musicCursor.getColumnIndex(Media._ID);
                int artistColumn = musicCursor.getColumnIndex(Media.ARTIST);
                int albumColumn = musicCursor.getColumnIndex(Media.ALBUM);
                int albumIdColumn = musicCursor.getColumnIndex(Media.ALBUM_ID);
                //add songs to list
                do {
                    long id = musicCursor.getLong(idColumn);

                    if (songsIds == null || songsIds.contains(id)) {
                        songs.add(new Song(id, musicCursor.getString(titleColumn), musicCursor.getString(artistColumn), musicCursor.getString(albumColumn), musicCursor.getString(albumIdColumn)));
                    }
                } while (musicCursor.moveToNext());
            }
        }
        Log.d(TAG, "listSongs: " + (System.currentTimeMillis() - start));
        return songs;
    }

    @NonNull
    private static Queue<Playlist> getPlaylists(@NonNull ContentResolver contentResolver) {
        long start = System.currentTimeMillis();
        Queue<Playlist> playlists = new ArrayDeque<>();
        playlistsSharedPreferences.getAll().forEach((name, songsIds) -> {
            ArrayList<Long> songsIdsList = new Gson().fromJson(songsIds.toString(), new TypeToken<ArrayList<Long>>() {}.getType());
            ArrayList<Song> songs = listSongs(contentResolver, SORT_DEFAULT, songsIdsList);
            Collections.sort(songs, Comparator.comparing(song -> songsIdsList.indexOf(song.songId)));
            playlists.add(new Playlist(name, songs));
        });
        Log.d(TAG, "getPlaylists: " + (System.currentTimeMillis() - start));
        return playlists;
    }

    public static void setPlaylists(@NonNull Queue<Playlist> playlists) {
        SharedPreferences.Editor editor = playlistsSharedPreferences.edit().clear();
        for (Playlist playlist : playlists) {
            editor.putString(playlist.name, new Gson().toJson((Object)playlist.songs.stream().map(song -> song.songId).collect(Collectors.toCollection(ArrayList::new))));
        }
        editor.apply();
    }

    @NonNull
    public static ArrayList<Song> listSongsByLastAdded(@NonNull ContentResolver contentResolver) {
        return listSongs(contentResolver, Media.DATE_ADDED + " DESC");
    }
}
