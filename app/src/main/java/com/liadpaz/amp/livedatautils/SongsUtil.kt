package com.liadpaz.amp.livedatautils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.liadpaz.amp.ui.viewmodels.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

object SongsUtil {
    private val songs = MutableLiveData(ArrayList<Song>())
    private val albumsLiveData = MutableLiveData(HashMap<String, ArrayList<Song>>())
    private val artistsLiveData = MutableLiveData(HashMap<String, ArrayList<Song>>())

    fun observeSongs(lifecycleOwner: LifecycleOwner, observer: Observer<ArrayList<Song>>) {
        songs.observe(lifecycleOwner, observer)
    }

    fun observeAlbums(lifecycleOwner: LifecycleOwner, observer: Observer<HashMap<String, ArrayList<Song>>>) {
        albumsLiveData.observe(lifecycleOwner, observer)
    }

    fun observeArtists(lifecycleOwner: LifecycleOwner, observer: Observer<HashMap<String, ArrayList<Song>>>) {
        artistsLiveData.observe(lifecycleOwner, observer)
    }

    val albums: HashMap<String, ArrayList<Song>>
        get() = albumsLiveData.value!!

    val artists: HashMap<String, ArrayList<Song>>
        get() = artistsLiveData.value!!

    @JvmStatic
    fun getSongs(): ArrayList<Song> {
        return songs.value!!
    }

    @JvmStatic
    fun setSongs(songs: ArrayList<Song>) {
        SongsUtil.songs.postValue(songs)
        CoroutineScope(Dispatchers.IO).launch {
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
            albumsLiveData.postValue(albumsMap)
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
            artistsLiveData.postValue(artistsMap)
        }
    }
}