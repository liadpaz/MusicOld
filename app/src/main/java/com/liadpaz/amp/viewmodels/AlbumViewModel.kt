package com.liadpaz.amp.viewmodels

import android.app.Application
import android.content.ComponentName
import android.support.v4.media.session.MediaControllerCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liadpaz.amp.model.repositories.SongsRepository
import com.liadpaz.amp.server.service.MediaPlayerService
import com.liadpaz.amp.server.service.ServiceConnection
import com.liadpaz.amp.server.utils.ALBUM
import com.liadpaz.amp.server.utils.ARTIST
import com.liadpaz.amp.server.utils.EXTRA_SHUFFLE
import com.liadpaz.amp.server.utils.buildMediaId
import com.liadpaz.amp.view.data.Song

class AlbumViewModel(application: Application, private val album: String) : AndroidViewModel(application) {

    private val repository = SongsRepository.getInstance(application)

    val transportControls: MediaControllerCompat.TransportControls

    private val serviceConnection = ServiceConnection.getInstance(application, ComponentName(application, MediaPlayerService::class.java)).also {
        transportControls = it.transportControls
    }

    fun playShuffle() =
            transportControls.playFromMediaId(buildMediaId(ALBUM, album, 0), bundleOf(Pair(EXTRA_SHUFFLE, true)))

    fun play(index: Int) =
            transportControls.playFromMediaId(buildMediaId(ALBUM, album, index), null)

    fun addToQueue(song: Song, index: Int = repository.getQueue().value!!.size) =
            serviceConnection.addToQueue(song, index)

    fun addToNext(song: Song) =
            serviceConnection.addToQueue(song, repository.getPosition().value!! + 1)

    class Factory(private val application: Application, private val album: String) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                AlbumViewModel(application, album) as T
    }
}