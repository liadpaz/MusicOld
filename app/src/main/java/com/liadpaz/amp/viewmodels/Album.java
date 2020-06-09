package com.liadpaz.amp.viewmodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Album implements Parcelable {
    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    public final String name;
    public final String artist;
    public final ArrayList<Song> songs;

    public Album(@NonNull String name, @NonNull String artist, @NonNull ArrayList<Song> songs) {
        this.name = name;
        this.artist = artist;
        this.songs = songs;
    }

    private Album(@NonNull Parcel in) {
        name = in.readString();
        artist = in.readString();
        songs = in.createTypedArrayList(Song.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(artist);
        dest.writeTypedList(songs);
    }
}
