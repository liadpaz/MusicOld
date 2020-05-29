package com.liadpaz.amp.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.liadpaz.amp.viewmodels.Playlist;

import java.util.ArrayList;

public class PlaylistsUtil {
    public static MutableLiveData<ArrayList<Playlist>> playlists = new MutableLiveData<>();

    @SuppressWarnings({"ConstantConditions", "BooleanMethodIsAlwaysInverted"})
    public static boolean isPlaylistExists(String name) {
        for (Playlist playlist : playlists.getValue()) {
            if (playlist.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    public static void addPlaylist(@NonNull Playlist playlist) {
        ArrayList<Playlist> playlists = PlaylistsUtil.playlists.getValue();
        playlists.add(playlist);
        PlaylistsUtil.playlists.setValue(playlists);
    }

    @SuppressWarnings("ConstantConditions")
    public static void removePlaylist(@NonNull String name) {
        ArrayList<Playlist> playlists = PlaylistsUtil.playlists.getValue();
        playlists.removeIf(playlist -> playlist.name.equals(name));
        PlaylistsUtil.playlists.setValue(playlists);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static Playlist getPlaylistByName(@NonNull String name) {
        for (Playlist playlist : playlists.getValue()) {
            if (playlist.name.equals(name)) {
                return playlist;
            }
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static ArrayList<String> getPlaylistsNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Playlist playlist : playlists.getValue()) {
            names.add(playlist.name);
        }
        return names;
    }
}
