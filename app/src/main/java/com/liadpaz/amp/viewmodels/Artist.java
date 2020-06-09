package com.liadpaz.amp.viewmodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Artist implements Parcelable {
    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @NonNull
        @Override
        public Artist createFromParcel(@NonNull Parcel in) {
            return new Artist(in);
        }

        @NonNull
        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    public final String name;
    public final ArrayList<Song> songs;

    public Artist(@NonNull String name, @NonNull ArrayList<Song> songs) {
        this.name = name;
        this.songs = songs;
    }

    private Artist(@NonNull Parcel in) {
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
