package com.liadpaz.amp.viewmodels

import android.app.Application
import android.content.ComponentName
import android.support.v4.media.session.MediaControllerCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.liadpaz.amp.model.repositories.SongsRepository
import com.liadpaz.amp.server.service.MediaPlayerService
import com.liadpaz.amp.server.service.ServiceConnection
import com.liadpaz.amp.server.utils.ALL
import com.liadpaz.amp.server.utils.EXTRA_SHUFFLE
import com.liadpaz.amp.server.utils.buildMediaId
import com.liadpaz.amp.view.data.Song

class SongListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SongsRepository.getInstance(application)

    fun getSongs(): LiveData<MutableList<Song>> = _songs

    private val _songs = repository.getSongs()
    private val _queue = repository.getQueue()
    private val _position = repository.getPosition()

    val transportControls: MediaControllerCompat.TransportControls

    private val serviceConnection = ServiceConnection.getInstance(application, ComponentName(application, MediaPlayerService::class.java)).also {
        transportControls = it.transportControls
    }

    fun playShuffle() {
        transportControls.playFromMediaId(buildMediaId(ALL, ALL, 0), bundleOf(Pair(EXTRA_SHUFFLE, true)))
    }

    fun addToQueue(song: Song, index: Int = _queue.value!!.size) {
        serviceConnection.addToQueue(song, index)
    }

    fun addToNext(song: Song) {
        serviceConnection.addToQueue(song, _position.value!! + 1)
    }
}