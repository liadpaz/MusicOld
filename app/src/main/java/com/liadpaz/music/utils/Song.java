package com.liadpaz.music.utils;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class Song {
    private String songName;
    private ArrayList<String> artists;
    private Drawable cover;

    public Song(String songName, ArrayList<String> artists, Drawable cover) {
        this.songName = songName;
        this.artists = artists;
        this.cover = cover;
    }

    public String getSongName() {
        return songName;
    }

    public ArrayList<String> getArtists() {
        return artists;
    }

    public Drawable getCover() {
        return cover;
    }
}
