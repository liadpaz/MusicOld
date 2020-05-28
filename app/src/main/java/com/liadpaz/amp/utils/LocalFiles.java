package com.liadpaz.amp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.liadpaz.amp.R;
import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LocalFiles {
    @SuppressWarnings("unused")
    private static final String TAG = "LOCAL_FILES";

    private static final String[] PROJECTION = {Media.TITLE, Media._ID, Media.ARTIST, Media.ALBUM, Media.ALBUM_ID};

    private static ArrayList<Song> allSongs;

    private static SharedPreferences musicSharedPreferences;
    private static SharedPreferences playlistsSharedPreferences;
    private static HashMap<String, ArrayList<Song>> artists = new HashMap<>();
    private static HashMap<String, ArrayList<Song>> albums = new HashMap<>();

    public LocalFiles(@NonNull SharedPreferences musicSharedPreferences, @NonNull SharedPreferences playlistsSharedPreferences) {
        LocalFiles.musicSharedPreferences = musicSharedPreferences;
        LocalFiles.playlistsSharedPreferences = playlistsSharedPreferences;

        QueueUtil.queue.observeForever(songs -> {
            String songsIds = new Gson().toJson((Object)songs.stream().map(song -> song.songId).collect(Collectors.toCollection(ArrayList::new)));
            musicSharedPreferences.edit().putString(Constants.SHARED_PREFERENCES_QUEUE, songsIds).apply();
        });
        QueueUtil.queuePosition.observeForever(queuePosition -> musicSharedPreferences.edit().putInt(Constants.SHARED_PREFERENCES_QUEUE, queuePosition).apply());
    }

    /**
     * This function returns the music folder path, if it does not exists, it returns the default one
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

    private static ArrayList<Song> listSongs(@NonNull Context context, @NonNull String sort) {
        long start = System.currentTimeMillis();
        String songUntitled = context.getString(R.string.song_no_name);
        String noArtist = context.getString(R.string.song_no_artist);
        String noAlbum = context.getString(R.string.song_no_album);
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
                    if (title == null) {
                        title = songUntitled;
                    }
                    String artist = musicCursor.getString(artistColumn);
                    String album = musicCursor.getString(albumColumn);
                    if (album == null) {
                        album = noAlbum;
                    }
                    String albumId = musicCursor.getString(albumIdColumn);

                    ArrayList<String> artists = new ArrayList<>();
                    if (artist != null && !artist.isEmpty()) {
                        Matcher matcher = Pattern.compile("([^ &,]([^,&])*[^ ,&]+)").matcher(artist);
                        while (matcher.find()) {
                            artists.add(matcher.group());
                        }
                    } else {
                        artists.add(noArtist);
                    }

                    songs.add(new Song(id, title, artists, album, albumId));
                } while (musicCursor.moveToNext());
            }
        }
        Log.d(TAG, "listSongs: " + (System.currentTimeMillis() - start));
        return songs;
    }

    @NonNull
    public static ArrayList<Song> listSongsByName(@NonNull Context context) {
        LocalFiles.allSongs = listSongs(context, Media.TITLE + " COLLATE NOCASE");
        setArtists();
        setAlbums();
        return LocalFiles.allSongs;
    }

    @NonNull
    public static ArrayList<Song> listSongsByLastAdded(@NonNull Context context) {
        return listSongs(context, Media.DATE_ADDED + " DESC");
    }

    @SuppressWarnings("ConstantConditions")
    private static void setArtists() {
        if (artists.size() != 0) {
            artists.clear();
        }
        for (final Song song : allSongs) {
            for (String artist : song.songArtists) {
                if (artists.containsKey(artist)) {
                    artists.get(artist).add(song);
                } else {
                    artists.put(artist, new ArrayList<Song>() {{
                        add(song);
                    }});
                }
            }
        }
    }

    @NonNull
    public static HashMap<String, ArrayList<Song>> getArtists() { return artists; }

    @SuppressWarnings("ConstantConditions")
    private static void setAlbums() {
        if (albums.size() != 0) {
            albums.clear();
        }
        for (final Song song : allSongs) {
            if (albums.containsKey(song.album)) {
                albums.get(song.album).add(song);
            } else {
                albums.put(song.album, new ArrayList<Song>() {{
                    add(song);
                }});
            }
        }
    }

    @NonNull
    public static HashMap<String, ArrayList<Song>> getAlbums() { return albums; }

    //    @NonNull
    //    public ArrayList<Song> getQueueFromLocal(@NonNull Context context) {
    //        ArrayList<Long> songsIdsList = new Gson().fromJson(musicSharedPreferences.getString(Constants.SHARED_PREFERENCES_QUEUE, "[]"), new TypeToken<ArrayList<Long>>() {}.getType());
    //        for (long id : songsIdsList) {
    //         if ()
    //        }
    //    }
}
