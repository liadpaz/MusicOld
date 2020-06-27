package com.liadpaz.amp.viewmodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liadpaz.amp.utils.Utilities;

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

    public final long songId;
    public final String title;
    public final ArrayList<String> artists;
    public final String album;
    public final String albumId;

    public Song(long songId, @NonNull String title, @NonNull String artists, @NonNull String album, @NonNull String albumId) {
        this.songId = songId;
        this.title = title;
        this.artists = Utilities.getArtistsFromSong(title, artists);
        this.album = album;
        this.albumId = albumId;
    }

    private Song(@NonNull Parcel in) {
        title = in.readString();
        artists = new ArrayList<>();
        in.readStringList(artists);
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
        return album.equals(other.album) && albumId.equals(other.albumId) && artists.equals(other.artists) && songId == other.songId && title.equals(other.title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeStringList(artists);
        dest.writeLong(songId);
        dest.writeString(album);
        dest.writeString(albumId);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("title: %s\nalbum: %s", title, album);
    }
}