package com.liadpaz.amp.livedatautils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.liadpaz.amp.viewmodels.Playlist;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class PlaylistsUtil {
    private static final String TAG = "PlaylistsUtil";

    private static MutableLiveData<ConcurrentLinkedDeque<Playlist>> playlists = new MutableLiveData<>();
    private static AtomicBoolean isChanging = new AtomicBoolean(false);

    @SuppressWarnings({"ConstantConditions", "BooleanMethodIsAlwaysInverted"})
    public static boolean isPlaylistExists(String name) {
        for (Playlist playlist : playlists.getValue()) {
            if (playlist.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<Queue<Playlist>> observer) {
        playlists.observe(lifecycleOwner, observer);
    }

    public static Deque<Playlist> getPlaylists() {
        return playlists.getValue();
    }

    public static void setPlaylists(@NonNull Queue<Playlist> playlists) {
        PlaylistsUtil.playlists.postValue(new ConcurrentLinkedDeque<>(playlists));
    }

    public static void addPlaylist(@NonNull Playlist playlist) {
        Deque<Playlist> playlists = getPlaylists();
        playlists.addFirst(playlist);
        setPlaylists(playlists);
    }

    @Nullable
    public static Playlist removePlaylist(@NonNull String name) {
        Queue<Playlist> playlists = getPlaylists();
        for (Playlist playlist : playlists) {
            if (playlist.name.equals(name)) {
                playlists.remove(playlist);
                setPlaylists(playlists);
                return playlist;
            }
        }
        return null;
    }

    @Nullable
    public static Playlist getPlaylistByName(@NonNull String name) {
        for (Playlist playlist : getPlaylists()) {
            if (name.equals(playlist.name)) {
                return playlist;
            }
        }
        return null;
    }

    @NonNull
    public static ArrayList<String> getPlaylistsNames() {
        return getPlaylists().stream().map(playlist -> playlist.name).collect(Collectors.toCollection(ArrayList::new));
    }

    public static boolean getIsChanging() {
        return isChanging.get();
    }

    public static void setIsChanging(boolean isChanging) {
        PlaylistsUtil.isChanging.set(isChanging);
    }
}
