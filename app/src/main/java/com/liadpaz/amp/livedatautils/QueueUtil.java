package com.liadpaz.amp.livedatautils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.liadpaz.amp.viewmodels.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueueUtil {
    private static final String TAG = "AmpApp.QueueUtil";

    private static final MutableLiveData<ArrayList<Song>> queue = new MutableLiveData<>(new ArrayList<>());
    private static final MutableLiveData<Integer> queuePosition = new MutableLiveData<>(-1);
    private static final AtomicBoolean isChanging = new AtomicBoolean(false);

    public static boolean getIsChanging() {
        return isChanging.get();
    }

    public static void setIsChanging(final boolean isChanging) {
        Log.d(TAG, "setIsChanging() called with: isChanging = [" + isChanging + "]");
        QueueUtil.isChanging.set(isChanging);
    }

    @SuppressWarnings("ConstantConditions")
    public static int getQueueSize() {
        return queue.getValue().size();
    }

    public static void observePosition(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<Integer> observer) {
        queuePosition.observe(lifecycleOwner, observer);
    }

    public static void observePosition(@NonNull Observer<Integer> observer) {
        queuePosition.observeForever(observer);
    }

    public static void observeQueue(@NonNull Observer<ArrayList<Song>> observer) {
        queue.observeForever(observer);
    }

    public static void observeQueue(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ArrayList<Song>> observer) {
        queue.observe(lifecycleOwner, observer);
    }

    public static void removePositionObserver(@NonNull Observer<Integer> observer) {
        queuePosition.removeObserver(observer);
    }

    public static void removeQueueObserver(@NonNull Observer<ArrayList<Song>> observer) {
        queue.removeObserver(observer);
    }

    @SuppressWarnings("ConstantConditions")
    public static int getPosition() {
        return queuePosition.getValue();
    }

    public static void setPosition(int position) {
        queuePosition.postValue(position);
    }

    public static ArrayList<Song> getQueue() {
        return queue.getValue();
    }

    public static void setQueue(@NonNull List<Song> queue) {
        QueueUtil.queue.postValue(new ArrayList<>(queue));
    }
}
