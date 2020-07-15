package com.liadpaz.amp.model.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.liadpaz.amp.model.utils.LocalFiles
import com.liadpaz.amp.view.data.Song

class MainRepository private constructor(application: Application) {

    private val localFiles: LocalFiles = LocalFiles.getInstance(application)

    private val path = MutableLiveData<String>().apply {
        postValue(localFiles.path)
    }
    private val screenOn = MutableLiveData<Boolean>().apply {
        postValue(localFiles.screenOn)
    }
    private val stopOnTask = MutableLiveData<Boolean>().apply {
        postValue(localFiles.stopOnTask)
    }


    init {
        path.observeForever {
            localFiles.path = it
            SongsRepository.getInstance(application).also { repo ->
                repo.getSongs()
                repo.getRecentlyAddedPlaylist()
            }
        }
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

    fun getPath(): MutableLiveData<String> = path

    fun getScreenOn(): MutableLiveData<Boolean> = screenOn

    fun getStopOnTask(): MutableLiveData<Boolean> = stopOnTask

    companion object {

        @Volatile
        private lateinit var instance: MainRepository

        @JvmStatic
        fun getInstance(application: Application): MainRepository {
            synchronized(MainRepository::class.java) {
                if (!Companion::instance.isInitialized) {
                    instance = MainRepository(application)
                }
            }
            return instance
        }
    }
}

private const val TAG = "AmpApp.MainRepository"