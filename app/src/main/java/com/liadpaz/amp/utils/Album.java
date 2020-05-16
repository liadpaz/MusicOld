package com.liadpaz.amp.utils;

import java.util.ArrayList;

public class Album {
    public final String name;
    public final String artist;
    public final ArrayList<Song> songs;

    public Album(String name, String artist, ArrayList<Song> songs) {
        this.name = name;
        this.artist = artist;
        this.songs = songs;
    }
}
