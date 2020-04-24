package com.liadpaz.music.utils;

import java.util.ArrayList;

public class Utilities {
    public static String joinArtists(ArrayList<String> artists) {
        StringBuilder joinedArtists = new StringBuilder(artists.get(0));
        for (int i = 1; i < artists.size(); i++) {
            joinedArtists.append(", ").append(artists.get(i));
        }
        return joinedArtists.toString();
    }

}
