package com.liadpaz.amp.model.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.liadpaz.amp.model.utils.LocalFiles
import com.liadpaz.amp.view.data.Album
import com.liadpaz.amp.view.data.Artist
import com.liadpaz.amp.view.data.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class SongsRepository(application: Application) {

    private val localFiles: LocalFiles = LocalFiles.getInstance(application)

    private val songs: MutableLiveData<MutableList<Song>> = MutableLiveData()
    private val artists: MutableLiveData<MutableList<Artist>> = MutableLiveData()
    private val albums: MutableLiveData<MutableList<Album>> = MutableLiveData()
    private val playlists: MutableLiveData<LinkedHashMap<String, ArrayList<Song>>> = MutableLiveData()
    private val recentlyAddedPlaylist: MutableLiveData<MutableList<Song>> = MutableLiveData()

    private var queue: MutableLiveData<MutableList<Song>> = MutableLiveData(arrayListOf())
    private var position: MutableLiveData<Int> = MutableLiveData()

    /**
     * This function retrieves all the songs on the device in [LiveData] format so observers can be
     * notified when all the songs are retrieved.
     */
    fun getSongs(): LiveData<MutableList<Song>> {
        CoroutineScope(Dispatchers.IO).launch {
            songs.postValue(localFiles.listSongs())
        }
        return songs
    }

    /**
     * This function retrieves all the albums on the device in [LiveData] format so observers can be
     * notified when all the albums are retrieved.
     */
    fun getAlbums(): LiveData<MutableList<Album>> {
        if (songs.value!!.isEmpty()) {
            throw IllegalStateException("Songs isn't initialized yet")
        }
        CoroutineScope(Dispatchers.Default).launch {
            val albumsMap = hashMapOf<String, ArrayList<Song>>()
            for (song in songs.value!!) {
                if (albumsMap.containsKey(song.album)) {
                    albumsMap[song.album]?.add(song)
                } else {
                    albumsMap[song.album] = arrayListOf(song)
                }
            }
            albums.postValue(albumsMap.map { (album, songs) -> Album(album, songs[0].artists[0], songs) }.sortedBy { album: Album -> album.name.toLowerCase(Locale.ROOT) }.toMutableList())
        }
        return albums
    }

    /**
     * This function retrieves all the artists on the device in [LiveData] format so observers can
     * be notified when all the artists are retrieved.
     */
    fun getArtists(): LiveData<MutableList<Artist>> {
        if (songs.value!!.isEmpty()) {
            throw IllegalStateException("Songs isn't initialized yet")
        }
        CoroutineScope(Dispatchers.Default).launch {
            val artistsMap = hashMapOf<String, ArrayList<Song>>()
            for (song in songs.value!!) {
                for (artist in song.artists) {
                    if (artistsMap.containsKey(artist)) {
                        artistsMap[artist]?.add(song)
                    } else {
                        artistsMap[artist] = arrayListOf(song)
                    }
                }
            }
            artists.postValue(artistsMap.map { (artist, songs) -> Artist(artist, songs) }.sortedBy { artist: Artist -> artist.name.toLowerCase(Locale.ROOT) }.toMutableList())
        }
        return artists
    }

    /**
     * This function retrieves all the user's playlists in [LiveData] format so observers can be
     * notified when all the playlists are retrieved.
     */
    fun getPlaylists(): LiveData<LinkedHashMap<String, ArrayList<Song>>> {
        CoroutineScope(Dispatchers.IO).launch {
            playlists.postValue(localFiles.getPlaylists())
        }
        return playlists
    }

    /**
     * This function retrieves the "recently added" playlist in [LiveData] format in order to do
     * that asynchronously
     */
    fun getRecentlyAddedPlaylist(): LiveData<MutableList<Song>> {
        CoroutineScope(Dispatchers.IO).launch {
            recentlyAddedPlaylist.postValue(localFiles.listSongsByLastAdded())
        }
        return recentlyAddedPlaylist
    }

    fun addPlaylist(playlistName: String, songs: ArrayList<Song>?) {
        val playlistsMap = (playlists.value)?.apply {
            put(playlistName, songs ?: arrayListOf())
        }
        localFiles.addPlaylist(playlistName, songs ?: arrayListOf())
        playlists.postValue(playlistsMap)
    }

    fun deletePlaylist(playlistName: String) {
        val playlistsMap = playlists.value!!.apply {
            remove(playlistName)
        }
        localFiles.deletePlaylist(playlistName)
        playlists.postValue(playlistsMap)
    }

    fun renamePlaylist(prevName: String, newName: String) {
        val songs: ArrayList<Song>
        val playlistsMap = playlists.value!!.apply {
            songs = remove(prevName)!!
            put(newName, songs)
        }
        localFiles.deletePlaylist(prevName)
        localFiles.addPlaylist(newName, songs)
        playlists.postValue(playlistsMap)
    }

    fun addSongToPlaylist(playlistName: String, song: Song? = null, songs: List<Song>? = null) {
        val newSongs: ArrayList<Song>
        val playlistsMap = playlists.value!!.apply {
            remove(playlistName)!!.also {
                it.addAll(songs ?: listOf(song!!))
            }.also { newSongs = it }
            put(playlistName, newSongs)
        }
        localFiles.deletePlaylist(playlistName)
        localFiles.addPlaylist(playlistName, newSongs)
        playlists.postValue(playlistsMap)
    }

    fun removeSongFromPlaylist(playlistName: String, position: Int) {
        val newSongs: ArrayList<Song>
        val playlistsMap = playlists.value!!.apply {
            remove(playlistName)!!.also {
                it.removeAt(position)
            }.also {
                newSongs = it
            }
            put(playlistName, newSongs)
        }
        localFiles.deletePlaylist(playlistName)
        localFiles.addPlaylist(playlistName, newSongs)
        playlists.postValue(playlistsMap)
    }

    fun moveSongInPlaylist(playlistName: String, fromPosition: Int, toPosition: Int) {
        val newSongs: ArrayList<Song>
        val playlistsMap = playlists.value!!.apply {
            remove(playlistName)!!.also {
                Collections.swap(it, fromPosition, toPosition)
            }.also {
                newSongs = it
            }
            put(playlistName, newSongs)
        }
        localFiles.deletePlaylist(playlistName)
        localFiles.addPlaylist(playlistName, newSongs)
        playlists.postValue(playlistsMap)
    }

    fun getQueue() = queue

    fun getPosition() = position

    fun addSongToQueue(index: Int, song: Song) {
        val queueList = queue.value!!.apply {
            add(index, song)
        }
        localFiles.setQueue(queueList)
        this.queue.postValue(queueList)
    }

    fun removeSongFromQueue(index: Int) {
        val queueList = queue.value!!.apply {
            removeAt(index)
        }
        localFiles.setQueue(queueList)
        this.queue.postValue(queueList)
    }

    fun moveSongInQueue(fromPosition: Int, toPosition: Int) {
        val queueList = queue.value!!.apply {
            Collections.swap(this, fromPosition, toPosition)
        }
        localFiles.setQueue(queueList)
        this.queue.postValue(queueList)
    }

    fun clearQueue() {
        localFiles.setQueue(arrayListOf())
        queue.postValue(arrayListOf())
    }

    companion object {
        @Volatile
        private lateinit var instance: SongsRepository

        @JvmStatic
        fun getInstance(application: Application): SongsRepository {
            synchronized(SongsRepository::class.java) {
                if (!Companion::instance.isInitialized) {
                    instance = SongsRepository(application)
                }
            }
            return instance
        }
    }
}

private const val TAG = "AmpApp.SongsRepository"