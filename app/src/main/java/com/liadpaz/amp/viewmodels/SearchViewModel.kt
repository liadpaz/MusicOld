package com.liadpaz.amp.viewmodels

import android.app.Application
import android.content.ComponentName
import android.support.v4.media.session.MediaControllerCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liadpaz.amp.model.repositories.SongsRepository
import com.liadpaz.amp.server.service.MediaPlayerService
import com.liadpaz.amp.server.service.ServiceConnection
import com.liadpaz.amp.server.utils.EXTRA_POSITION
import com.liadpaz.amp.view.data.Song

class SearchViewModel(application: Application, private val query: String) : AndroidViewModel(application) {

    private val repository = SongsRepository.getInstance(application)

    private val transportControls: MediaControllerCompat.TransportControls

    private val _position = repository.getPosition()

    private val serviceConnection = ServiceConnection.getInstance(application, ComponentName(application, MediaPlayerService::class.java)).also {
        transportControls = it.transportControls
    }

    fun getSongs(): LiveData<MutableList<Song>> = repository.getSongs()

    fun play(position: Long) =
            transportControls.playFromSearch(query, bundleOf(Pair(EXTRA_POSITION, position)))

    fun addToQueue(song: Song) {
        serviceConnection.addToQueue(song)
    }

    fun addToNext(song: Song) {
        serviceConnection.addToQueue(song, _position.value!! + 1)
    }

    class Factory(private val application: Application, private val query: String) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                SearchViewModel(application, query) as T
    }
}