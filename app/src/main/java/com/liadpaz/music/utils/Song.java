package com.liadpaz.music.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Song {
    private String songName;
    private ArrayList<String> artists;
    private byte[] cover;
    private String path;

    public Song(@NonNull String songName, @NonNull ArrayList<String> artists, @Nullable byte[] cover, @NonNull String path) {
        this.songName = songName;
        this.artists = artists;
        this.cover = cover;
        this.path = path;
    }

    @NonNull
    public String getPath() { return path; }

    @NonNull
    public String getSongName() { return songName; }

    @NonNull
    public ArrayList<String> getArtists() { return artists; }

    @Nullable
    public byte[] getCover() { return cover; }
}
