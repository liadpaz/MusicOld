package com.liadpaz.amp.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.liadpaz.amp.viewmodels.Playlist;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PlaylistsUtil {
    private static final String TAG = "PlaylistsUtil";

    private static MutableLiveData<ArrayList<Playlist>> playlists = new MutableLiveData<>();

    @SuppressWarnings({"ConstantConditions", "BooleanMethodIsAlwaysInverted"})
    public static boolean isPlaylistExists(String name) {
        for (Playlist playlist : playlists.getValue()) {
            if (playlist.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ArrayList<Playlist>> observer) {
        playlists.observe(lifecycleOwner, observer);
    }

    public static ArrayList<Playlist> getPlaylists() {
        return playlists.getValue();
    }

    public static void setPlaylists(@NonNull ArrayList<Playlist> playlists) {
        PlaylistsUtil.playlists.postValue(playlists);
    }

    public static void addPlaylist(@NonNull Playlist playlist) {
        ArrayList<Playlist> playlists = getPlaylists();
        playlists.add(playlist);
        setPlaylists(playlists);
    }

    @Nullable
    public static Playlist removePlaylist(@NonNull String name) {
        ArrayList<Playlist> playlists = getPlaylists();
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
