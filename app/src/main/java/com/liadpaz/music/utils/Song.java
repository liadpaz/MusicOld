package com.liadpaz.music.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Song {
    private static final String TAG = "SONG";
    private String songName;
    private ArrayList<String> artists;
    private String path;

    private Bitmap cover = null;
    private boolean triedCover = false;

    public Song(@NonNull String songName, @NonNull ArrayList<String> artists, @NonNull String path) {
        this.songName = songName;
        this.artists = artists;
        this.path = path;
    }

    @NonNull
    public String getPath() { return path; }

    @NonNull
    public String getSongName() { return songName; }

    @NonNull
    public ArrayList<String> getArtists() { return artists; }

    @Nullable
    public Bitmap getCover() {
        if (!triedCover) {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            try {
                metadataRetriever.setDataSource(path);
                byte[] data;
                data = metadataRetriever.getEmbeddedPicture();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                BitmapFactory.decodeByteArray(data, 0, data.length).compress(Bitmap.CompressFormat.JPEG, 50, os);
                data = os.toByteArray();
                triedCover = true;
                return (cover = BitmapFactory.decodeByteArray(data, 0, data.length));
            } catch (Exception ignored) {
                return null;
            } finally {
                metadataRetriever.release();
            }
        } else {
            return cover;
        }
    }
}
