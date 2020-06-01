package com.liadpaz.amp.LiveDataUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;

public class QueueUtil {
    public static final MutableLiveData<ArrayList<Song>> queue = new MutableLiveData<>(new ArrayList<>());
    public static final MutableLiveData<Integer> queuePosition = new MutableLiveData<>(0);

    @SuppressWarnings("ConstantConditions")
    public static void addToEnd(@NonNull Song song) {
        ArrayList<Song> songs = queue.getValue();
        songs.add(song);
        queue.postValue(songs);
    }

    @SuppressWarnings("ConstantConditions")
    public static void addAtIndex(@NonNull Song song, int index) {
        ArrayList<Song> songs = queue.getValue();
        songs.add(index, song);
        queue.postValue(songs);
    }

    @SuppressWarnings("ConstantConditions")
    public static void addToNext(@NonNull Song song) {
        ArrayList<Song> songs = queue.getValue();
        if (songs.size() == 0) {
            queue.setValue(new ArrayList<Song>() {{
                add(song);
            }});
        } else {
            addAtIndex(song, queuePosition.getValue() + 1);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void addToPosition(int add) {
        queuePosition.setValue(queuePosition.getValue() + add);
    }

    public static void setPosition(int position) {
        queuePosition.postValue(position);
    }
}
