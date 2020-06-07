package com.liadpaz.amp.livedatautils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;

public class QueueUtil {
    private static final MutableLiveData<ArrayList<Song>> queue = new MutableLiveData<>(new ArrayList<>());
    private static final MutableLiveData<Integer> queuePosition = new MutableLiveData<>(-1);
    private static final String TAG = "AmpApp.QueueUtil";
    private static boolean isChanging = false;

    public static void setIsChanging(boolean isChanging) {
        Log.d(TAG, "setIsChanging: ");
        QueueUtil.isChanging = isChanging;
    }

    @SuppressWarnings("ConstantConditions")
    public static void addToEnd(@NonNull Song song) {
        ArrayList<Song> songs = queue.getValue();
        songs.add(song);
        queue.postValue(songs);
    }

    public static void observePosition(@NonNull Observer<Integer> observer) {
        queuePosition.observeForever(observer);
    }

    public static void observePosition(@NonNull LifecycleOwner owner, @NonNull Observer<Integer> observer) {
        queuePosition.observe(owner, observer);
    }

    @SuppressWarnings("ConstantConditions")
    public static int getQueueSize() {
        return queue.getValue().size();
    }

    @SuppressWarnings("ConstantConditions")
    public static int getPosition() {
        return queuePosition.getValue();
    }

    public static void setPosition(int position) {
        queuePosition.postValue(position);
    }

    public static void observeQueue(@NonNull LifecycleOwner owner, @NonNull Observer<ArrayList<Song>> observer) {
        queue.observe(owner, observer);
    }

    public static void observeQueue(@NonNull Observer<ArrayList<Song>> observer) {
        queue.observeForever(observer);
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static ArrayList<Song> getQueue() {
        return queue.getValue();
    }

    public static void setQueue(@NonNull ArrayList<Song> songs) {
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

    public static boolean isChanging() { return isChanging; }
}
