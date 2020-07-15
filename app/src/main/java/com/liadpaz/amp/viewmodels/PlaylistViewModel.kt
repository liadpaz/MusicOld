package com.liadpaz.amp.viewmodels

import android.app.Application
import android.content.ComponentName
import android.support.v4.media.session.MediaControllerCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import com.liadpaz.amp.R
import com.liadpaz.amp.model.repositories.SongsRepository
import com.liadpaz.amp.server.service.MediaPlayerService
import com.liadpaz.amp.server.service.ServiceConnection
import com.liadpaz.amp.server.utils.EXTRA_SHUFFLE
import com.liadpaz.amp.server.utils.PLAYLIST
import com.liadpaz.amp.server.utils.buildMediaId
import com.liadpaz.amp.view.data.Song

class PlaylistViewModel(application: Application, private val playlistName: String) : AndroidViewModel(application) {

    private val isRecentlyAdded = playlistName == application.getString(R.string.playlist_recently_added)

    private val recentlyAddedObserver: Observer<List<Song>> = Observer {
        playlistObservable.postValue(it)
    }
    private val playlistsObserver: Observer<LinkedHashMap<String, ArrayList<Song>>> = Observer { playlists ->
        playlistObservable.postValue(playlists[playlistName])
    }
    private val playlistObservable = MutableLiveData<List<Song>>()

    private val _position: LiveData<Int>

    private val transportControls: MediaControllerCompat.TransportControls

    private val serviceConnection = ServiceConnection.getInstance(application, ComponentName(application, MediaPlayerService::class.java)).also {
        transportControls = it.transportControls
    }

    private val repository = SongsRepository.getInstance(application).also {
        if (isRecentlyAdded) {
            it.getRecentlyAddedPlaylist().observeForever(recentlyAddedObserver)
        } else {
            it.getPlaylists().observeForever(playlistsObserver)
        }
        _position = it.getPosition()
    }

    fun getPlaylist(): LiveData<List<Song>> = playlistObservable

    fun play(position: Int) {
        transportControls.playFromMediaId(buildMediaId(PLAYLIST, playlistName, position), null)
    }

    fun playShuffle() {
        transportControls.playFromMediaId(buildMediaId(PLAYLIST, playlistName, 0), bundleOf(Pair(EXTRA_SHUFFLE, true)))
    }

    fun deletePlaylist() = repository.deletePlaylist(playlistName)

    fun removeSong(position: Int) = repository.removeSongFromPlaylist(playlistName, position)

    fun moveSongPosition(fromPosition: Int, toPosition: Int) = repository.moveSongInPlaylist(playlistName, fromPosition, toPosition)

    fun addToQueue(song: Song) {
        serviceConnection.addToQueue(song)
    }

    fun addToNext(song: Song) {
        serviceConnection.addToQueue(song, _position.value!! + 1)
    }

    override fun onCleared() {
        if (isRecentlyAdded) {
            repository.getRecentlyAddedPlaylist().removeObserver(recentlyAddedObserver)
        } else {
            repository.getPlaylists().removeObserver(playlistsObserver)
        }
    }

    class Factory(private val application: Application, private val playlistName: String) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                PlaylistViewModel(application, playlistName) as T
    }
}

private const val TAG = "AmpApp.PlaylistViewModel"