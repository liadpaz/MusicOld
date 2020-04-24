package com.liadpaz.music.utils;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Song {
    private String songName;
    private ArrayList<String> artists;
    private Bitmap cover;

    public Song(String songName, ArrayList<String> artists, Bitmap cover) {
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

    public Bitmap getCover() {
        return cover;
    }
}
