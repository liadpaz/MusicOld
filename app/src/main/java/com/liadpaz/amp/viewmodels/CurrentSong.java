package com.liadpaz.amp.viewmodels;

import androidx.annotation.NonNull;

public class CurrentSong {
    public final String title;
    public final String artists;

    public CurrentSong(@NonNull String title, @NonNull String artists) {
        this.title = title;
        this.artists = artists;
    }
}
