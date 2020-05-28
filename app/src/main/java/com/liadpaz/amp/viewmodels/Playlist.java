package com.liadpaz.amp.viewmodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Playlist implements Parcelable {
    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeTypedList(songs);
    }
}
