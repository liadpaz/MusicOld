package com.liadpaz.amp.utils;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Artist {
    public final String name;
    public final ArrayList<Song> songs;

    public Artist(@NonNull String name, @NonNull ArrayList<Song> songs) {
        this.name = name;
        this.songs = songs;
    }
}
