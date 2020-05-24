package com.liadpaz.amp.viewmodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Song implements Parcelable {
    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @NonNull
        @Override
        public Song createFromParcel(@NonNull Parcel in) {
            return new Song(in);
        }

        @NonNull
        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    private static final String TAG = "SONG";

    public final long songId;
    public final String songTitle;
    public final ArrayList<String> songArtists;
    public final String album;
    public final String albumId;

    public Song(long songId, @NonNull String songTitle,@NonNull ArrayList<String> songArtists,@NonNull String album,@NonNull String albumId) {
        this.songId = songId;
        this.songTitle = songTitle;
        this.songArtists = songArtists;
        this.album = album;
        this.albumId = albumId;
    }

    private Song(@NonNull Parcel in) {
        songTitle = in.readString();
        songArtists = new ArrayList<>();
        in.readStringList(songArtists);
        songId = in.readLong();
        album = in.readString();
        albumId = in.readString();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Song)) {
            return false;
        }
        Song other = (Song)obj;
        return album.equals(other.album) && albumId.equals(other.albumId) && songArtists.equals(other.songArtists) && songId == other.songId && songTitle.equals(other.songTitle);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(songTitle);
        dest.writeStringList(songArtists);
        dest.writeLong(songId);
        dest.writeString(album);
        dest.writeString(albumId);
    }
}