package com.liadpaz.amp.LiveDataUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;

public class SongsUtil {
    private static MutableLiveData<ArrayList<Song>> songs = new MutableLiveData<>(new ArrayList<>());

    public static void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ArrayList<Song>> observer) {
        songs.observe(lifecycleOwner, observer);
    }

    public static ArrayList<Song> getSongs() {
        return songs.getValue();
    }

    public static void setSongs(@NonNull ArrayList<Song> songs) {
        SongsUtil.songs.postValue(songs);
    }
}
