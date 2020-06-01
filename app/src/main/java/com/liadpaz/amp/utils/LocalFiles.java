package com.liadpaz.amp.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liadpaz.amp.LiveDataUtils.PlaylistsUtil;
import com.liadpaz.amp.LiveDataUtils.SongsUtil;
import com.liadpaz.amp.viewmodels.Playlist;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LocalFiles {
    @SuppressWarnings("unused")
    private static final String TAG = "LOCAL_FILES";

    private static final String[] PROJECTION = {Media.TITLE, Media._ID, Media.ARTIST, Media.ALBUM, Media.ALBUM_ID};

    private static SharedPreferences musicSharedPreferences;
    private static SharedPreferences playlistsSharedPreferences;

    public static void init(@NonNull Context context, @NonNull LifecycleOwner lifecycleOwner) {
        LocalFiles.musicSharedPreferences = context.getSharedPreferences("Music.Data", 0);
        LocalFiles.playlistsSharedPreferences = context.getSharedPreferences("Music.Playlists", 0);

        CompletableFuture.runAsync(() -> PlaylistsUtil.setPlaylists(getPlaylists(context)));
        CompletableFuture.runAsync(() -> SongsUtil.setSongs(listSongs(context, Media.TITLE + " COLLATE NOCASE")));

//        if (!lifecycleOwner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.INITIALIZED)) {
//            QueueUtil.queue.observe(lifecycleOwner, songs -> {
//                ArrayList<Long> songsIdsList = songs.stream().map(song -> song.songId).collect(Collectors.toCollection(ArrayList::new));
//                musicSharedPreferences.edit().putString(Constants.SHARED_PREFERENCES_QUEUE, new Gson().toJson(songsIdsList)).apply();
//            });
//            QueueUtil.queuePosition.observe(lifecycleOwner, queuePosition -> musicSharedPreferences.edit().putInt(Constants.SHARED_PREFERENCES_QUEUE, queuePosition).apply());
//        }

        // TODO: fix queue saving
    }

    /**
     * This function returns the music folder path, if it does not exists, it returns the default
     * one
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

    @NonNull
    private static ArrayList<Song> listSongs(@NonNull Context context, @NonNull String sort) {
        long start = System.currentTimeMillis();
        ArrayList<Song> songs = new ArrayList<>();
        //retrieve song info
        ContentResolver musicResolver = context.getContentResolver();
        try (Cursor musicCursor = musicResolver.query(Media.EXTERNAL_CONTENT_URI, PROJECTION, "_data like ?", new String[]{"%" + getPath() + "%"}, sort)) {
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
                    String title = musicCursor.getString(titleColumn);
                    String artist = musicCursor.getString(artistColumn);
                    String album = musicCursor.getString(albumColumn);
                    String albumId = musicCursor.getString(albumIdColumn);

                    ArrayList<String> artists = new ArrayList<>();
                    Matcher matcher = Pattern.compile("([^ &,]([^,&])*[^ ,&]+)").matcher(artist);
                    while (matcher.find()) {
                        artists.add(matcher.group());
                    }
                    songs.add(new Song(id, title, artists, album, albumId));
                } while (musicCursor.moveToNext());
            }
        }
        Log.d(TAG, "listSongs: " + (System.currentTimeMillis() - start));
        return songs;
    }

    @NonNull
    private static ArrayList<Playlist> getPlaylists(@NonNull Context context) {
        ArrayList<Playlist> playlists = new ArrayList<>();
        playlistsSharedPreferences.getAll().forEach((name, songsIds) -> {
            ArrayList<Long> songsIdsList = new Gson().fromJson(songsIds.toString(), new TypeToken<ArrayList<Long>>() {}.getType());
            playlists.add(new Playlist(name, songsIdsList.stream().filter(id -> isSongExists(context, id)).map(id -> getSongById(context, id)).collect(Collectors.toCollection(ArrayList::new))));
        });
        return playlists;
    }

    public static void setPlaylists(@NonNull List<Playlist> playlists) {
        SharedPreferences.Editor editor = playlistsSharedPreferences.edit().clear();
        for (Playlist playlist : playlists) {
            ArrayList<Long> songsIdsList = playlist.songs.stream().map(song -> song.songId).collect(Collectors.toCollection(ArrayList::new));
            editor.putString(playlist.name, new Gson().toJson(songsIdsList));
        }
        editor.apply();
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean isSongExists(@NonNull Context context, long id) {
        try (Cursor cursor = context.getContentResolver().query(ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id), null, null, null)) {
            return cursor.moveToFirst();
        } catch (Exception ignored) {
            return false;
        }
    }

    @NonNull
    public static ArrayList<Song> listSongsByLastAdded(@NonNull Context context) {
        return listSongs(context, Media.DATE_ADDED + " DESC");
    }

    /**
     * This function returns the song by its id in the {@link android.provider.MediaStore}
     *
     * @param context context
     * @param id      song id
     * @return a song with the {@param id} as its id in the {@link android.provider.MediaStore}
     */
    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static Song getSongById(@NonNull Context context, long id) {
        try (Cursor songCursor = context.getContentResolver().query(ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id), PROJECTION, null, null, null)) {
            if (songCursor.moveToFirst()) {
                String title = songCursor.getString(songCursor.getColumnIndex(Media.TITLE));
                String artist = songCursor.getString(songCursor.getColumnIndex(Media.ARTIST));
                String album = songCursor.getString(songCursor.getColumnIndex(Media.ALBUM));
                String albumId = songCursor.getString(songCursor.getColumnIndex(Media.ALBUM_ID));

                ArrayList<String> artists = new ArrayList<>();
                Matcher matcher = Pattern.compile("([^ &,]([^,&])*[^ ,&]+)").matcher(artist);
                while (matcher.find()) {
                    artists.add(matcher.group());
                }
                return new Song(id, title, artists, album, albumId);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    //    @NonNull
    //    public ArrayList<Song> getQueueFromLocal(@NonNull Context context) {
    //        ArrayList<Long> songsIdsList = new Gson().fromJson(musicSharedPreferences.getString(Constants.SHARED_PREFERENCES_QUEUE, "[]"), new TypeToken<ArrayList<Long>>() {}.getType());
    //        for (long id : songsIdsList) {
    //         if ()
    //        }
    //    }
}
