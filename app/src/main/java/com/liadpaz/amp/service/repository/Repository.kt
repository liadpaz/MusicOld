package com.liadpaz.amp.service.repository

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.liadpaz.amp.service.model.LocalFiles
import com.liadpaz.amp.service.server.service.MediaPlayerService
import com.liadpaz.amp.service.server.service.ServiceConnection
import com.liadpaz.amp.view.data.Album
import com.liadpaz.amp.view.data.Artist
import com.liadpaz.amp.view.data.Playlist
import com.liadpaz.amp.view.data.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class Repository private constructor(context: Context) {

    private val localFiles: LocalFiles = LocalFiles.getInstance(context)

    private val serviceConnector = ServiceConnection.getInstance(context, ComponentName(context, MediaPlayerService::class.java))

    private val songs: MutableLiveData<List<Song>> = MutableLiveData(listOf())
    private val artists: MutableLiveData<List<Artist>> = MutableLiveData(listOf())
    private val albums: MutableLiveData<List<Album>> = MutableLiveData(listOf())
    private val playlists: MutableLiveData<Queue<Playlist>> = MutableLiveData(ArrayDeque())
    private val recentlyAddedPlaylist: MutableLiveData<ArrayList<Song>> = MutableLiveData(arrayListOf())

    private val path: MutableLiveData<String> = MutableLiveData(localFiles.path)
    private val screenOn: MutableLiveData<Boolean> = MutableLiveData(localFiles.screenOn)

    private var queue: MutableLiveData<List<Song>> = MutableLiveData(arrayListOf())
    private var position: MutableLiveData<Int> = MutableLiveData()
    private var currentSong: MutableLiveData<Song> = MutableLiveData()

    private var currentColor: MutableLiveData<Int> = MutableLiveData()
    private var lastColor: MutableLiveData<Int> = MutableLiveData()


    init {
        path.observeForever {
            localFiles.path = it
            getSongs()
            getPlaylists()
        }
    }

    /**
     * This function retrieves all the songs on the device in [LiveData] format so observers can be
     * notified when all the songs are retrieved.
     */
    fun getSongs(): LiveData<List<Song>> {
        CoroutineScope(Dispatchers.IO).launch {
            songs.postValue(localFiles.listSongs())
        }
        return songs
    }

    /**
     * This function retrieves all the albums on the device in [LiveData] format so observers can be
     * notified when all the albums are retrieved.
     */
    fun getAlbums(): LiveData<List<Album>> {
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
            albums.postValue(albumsMap.map { (album, songs) -> Album(album, songs[0].artists[0], songs) }.sortedBy { album: Album -> album.name.toLowerCase(Locale.ROOT) })
        }
        return albums
    }

    /**
     * This function retrieves all the artists on the device in [LiveData] format so observers can
     * be notified when all the artists are retrieved.
     */
    fun getArtists(): LiveData<List<Artist>> {
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
            artists.postValue(artistsMap.map { (artist, songs) -> Artist(artist, songs) }.sortedBy { artist: Artist -> artist.name.toLowerCase(Locale.ROOT) })
        }
        return artists
    }

    /**
     * This function retrieves all the user's playlists in [LiveData] format so observers can be
     * notified when all the playlists are retrieved.
     */
    fun getPlaylists(): LiveData<Queue<Playlist>> {
        CoroutineScope(Dispatchers.IO).launch {
            playlists.postValue(localFiles.getPlaylists())
        }
        return playlists
    }

//    /**
//     * This function retrieves an artist from the artists collection in [LiveData] format in order
//     * to do that asynchronously
//     */
//    fun getArtist(artistName: String): LiveData<Artist?> {
//        val data = MutableLiveData<Artist?>()
//        CoroutineScope(Dispatchers.Default).launch {
//            data.postValue(artists.value!!.find { artist -> artist.name == artistName })
//        }
//        return data
//    }
//
//    /**
//     * This function retrieves an album from the albums collection in [LiveData] format in order
//     * to do that asynchronously
//     */
//    fun getAlbum(albumName: String): LiveData<Album?> {
//        val data = MutableLiveData<Album?>()
//        CoroutineScope(Dispatchers.Default).launch {
//            data.postValue(albums.value!!.find { album -> album.name == albumName })
//        }
//        return data
//    }
//
//    /**
//     * This function retrieves a playlist from the playlists collection in [LiveData] format in
//     * order to do that asynchronously
//     */
//    fun getPlaylist(playlistName: String): LiveData<Playlist?> {
//        val data = MutableLiveData<Playlist?>()
//        CoroutineScope(Dispatchers.Default).launch {
//            data.postValue(playlists.value!!.find { playlist -> playlist.name == playlistName })
//        }
//        return data
//    }

    /**
     * This function retrieves the "recently added" playlist in [LiveData] format in order to do
     * that asynchronously
     */
    fun getRecentlyAddedPlaylist(): LiveData<ArrayList<Song>> {
        CoroutineScope(Dispatchers.IO).launch {
            recentlyAddedPlaylist.postValue(localFiles.listSongsByLastAdded())
        }
        return recentlyAddedPlaylist
    }

    fun getQueue(): LiveData<List<Song>> {
        // TODO: get live queue
        return queue
    }

    fun getPosition(): LiveData<Int> {
        // TODO: get live position
        return position
    }

    fun getPath(): MutableLiveData<String> = path

    fun getScreenOn(): MutableLiveData<Boolean> = screenOn

    companion object {
        private const val TAG = "AmpApp.Repository"

        @Volatile
        private var repository: Repository? = null

        @JvmStatic
        fun getInstance(context: Context): Repository {
            synchronized(Repository::class.java) {
                if (repository == null) {
                    repository = Repository(context)
                }
            }
            return repository!!
        }
    }
}