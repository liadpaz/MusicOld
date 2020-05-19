package com.liadpaz.amp.utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Song implements Parcelable {
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @SuppressWarnings("unchecked")
        public Song createFromParcel(Parcel in) {
            Song song = new Song();
            song.songTitle = in.readString();
            song.songArtists = in.readArrayList(String.class.getClassLoader());
            song.songId = in.readLong();
            song.album = in.readString();
            song.albumId = in.readString();
            return song;
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    private static final String TAG = "SONG";

    private long songId;
    private String songTitle;
    private ArrayList<String> songArtists;
    private String album;
    private String albumId;

    Song(long songId, String songTitle, ArrayList<String> songArtists, String album, String albumId) {
        this.songId = songId;
        this.songTitle = songTitle;
        this.songArtists = songArtists;
        this.album = album;
        this.albumId = albumId;
    }

    private Song() {}

    public long getSongId() {return songId;}

    public String getSongTitle() {return songTitle;}

    public ArrayList<String> getSongArtists() {return songArtists;}

    public String getAlbum() {return album;}

    String getAlbumId() {return albumId;}

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
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(songTitle);
        dest.writeList(songArtists);
        dest.writeLong(songId);
        dest.writeString(album);
        dest.writeString(albumId);
    }
}