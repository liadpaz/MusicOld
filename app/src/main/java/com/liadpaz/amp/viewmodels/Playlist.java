package com.liadpaz.amp.viewmodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class Playlist implements Parcelable {
    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @NonNull
        @Override
        public Playlist createFromParcel(@NonNull Parcel in) {
            return new Playlist(in);
        }

        @NonNull
        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    public final String name;
    public final ArrayList<Song> songs;

    public Playlist(@NonNull String name, @NonNull ArrayList<Song> songs) {
        this.name = name;
        this.songs = songs;
    }

    private Playlist(@NonNull Parcel in) {
        name = in.readString();
        songs = in.createTypedArrayList(Song.CREATOR);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Playlist) {
            Playlist other = (Playlist)obj;
            return name.equals(other.name) && songs.equals(other.songs);
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeTypedList(songs);
    }

    @NonNull
    @Override
    public String toString() {
        return "name: " + name + "\nsongs: " + Arrays.deepToString(songs.toArray());
    }
}
