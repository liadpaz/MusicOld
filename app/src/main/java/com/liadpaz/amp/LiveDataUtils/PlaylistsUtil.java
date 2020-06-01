package com.liadpaz.amp.LiveDataUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.liadpaz.amp.viewmodels.Playlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PlaylistsUtil {
    private static final String TAG = "PlaylistsUtil";

    private static MutableLiveData<CopyOnWriteArrayList<Playlist>> playlists = new MutableLiveData<>(new CopyOnWriteArrayList<>());

    @SuppressWarnings({"ConstantConditions", "BooleanMethodIsAlwaysInverted"})
    public static boolean isPlaylistExists(String name) {
        for (Playlist playlist : playlists.getValue()) {
            if (playlist.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<CopyOnWriteArrayList<Playlist>> observer) {
        playlists.observe(lifecycleOwner, observer);
    }

    public static CopyOnWriteArrayList<Playlist> getPlaylists() {
        return playlists.getValue();
    }

    public static void setPlaylists(@NonNull List<Playlist> playlists) {
        PlaylistsUtil.playlists.postValue(new CopyOnWriteArrayList<>(playlists));
    }

    public static void addPlaylist(@NonNull Playlist playlist) {
        List<Playlist> playlists = getPlaylists();
        playlists.add(playlist);
        Collections.reverse(playlists);
        setPlaylists(playlists);
    }

    @Nullable
    public static Playlist removePlaylist(@NonNull String name) {
        CopyOnWriteArrayList<Playlist> playlists = getPlaylists();
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
}
