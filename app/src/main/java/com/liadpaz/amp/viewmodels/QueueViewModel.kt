package com.liadpaz.amp.viewmodels

import android.app.Application
import android.content.ComponentName
import android.support.v4.media.session.MediaControllerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.liadpaz.amp.model.repositories.SongsRepository
import com.liadpaz.amp.server.service.MediaPlayerService
import com.liadpaz.amp.server.service.ServiceConnection
import com.liadpaz.amp.view.data.Song

class QueueViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SongsRepository.getInstance(application)

    fun getQueue(): LiveData<MutableList<Song>> = _queue
    fun getPosition(): LiveData<Int> = _position

    private val transportControls: MediaControllerCompat.TransportControls

    private val _queue = repository.getQueue()
    private val _position = repository.getPosition()

    private val serviceConnection = ServiceConnection.getInstance(application, ComponentName(application, MediaPlayerService::class.java)).also {
        transportControls = it.transportControls
    }

    fun play(position: Long) = transportControls.skipToQueueItem(position)

    fun addToQueue(song: Song) {
        serviceConnection.addToQueue(song)
    }

    fun addToNext(song: Song) {
        serviceConnection.addToQueue(song, _position.value!! + 1)
    }

    fun removeSongFromQueue(index: Int) {
        serviceConnection.removeFromQueue(_queue.value!![index])
    }

    fun moveSong(from: Int, to: Int) {
        serviceConnection.moveSong(from, to)
    }

    fun clear() {
        serviceConnection.clearQueue()
    }
}