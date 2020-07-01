package com.liadpaz.amp.livedatautils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.liadpaz.amp.viewmodels.Song
import java.util.*
import java.util.concurrent.CompletableFuture

object SongsUtil {
    private val songs = MutableLiveData(ArrayList<Song>())
    private val albums = MutableLiveData(HashMap<String, ArrayList<Song>>())
    private val artists = MutableLiveData(HashMap<String, ArrayList<Song>>())
    fun observeSongs(lifecycleOwner: LifecycleOwner, observer: Observer<ArrayList<Song>>) {
        songs.observe(lifecycleOwner, observer)
    }

    fun observeAlbums(lifecycleOwner: LifecycleOwner, observer: Observer<HashMap<String, ArrayList<Song>>>) {
        albums.observe(lifecycleOwner, observer)
    }

    fun observeArtists(lifecycleOwner: LifecycleOwner, observer: Observer<HashMap<String, ArrayList<Song>>>) {
        artists.observe(lifecycleOwner, observer)
    }

    fun getAlbums(): HashMap<String, ArrayList<Song>> {
        return albums.value!!
    }

    fun getArtists(): HashMap<String, ArrayList<Song>> {
        return artists.value!!
    }

    @JvmStatic
    fun getSongs(): ArrayList<Song> {
        return songs.value!!
    }

    @JvmStatic
    fun setSongs(songs: ArrayList<Song>) {
        SongsUtil.songs.postValue(songs)
        CompletableFuture.runAsync {
            val albumsMap = HashMap<String, ArrayList<Song>>()
            for (song in songs) {
                if (albumsMap.containsKey(song.album)) {
                    albumsMap[song.album]!!.add(song)
                } else {
                    albumsMap[song.album] = ArrayList<Song>().apply {
                        add(song)
                    }
                }
            }
            albums.postValue(albumsMap)
        }
        CompletableFuture.runAsync {
            val artistsMap = HashMap<String, ArrayList<Song>>()
            for (song in songs) {
                for (artist in song.artists) {
                    if (artistsMap.containsKey(artist)) {
                        artistsMap[artist]!!.add(song)
                    } else {
                        artistsMap[artist] = ArrayList<Song>().apply {
                            add(song)
                        }
                    }
                }
            }
            artists.postValue(artistsMap)
        }
    }
}