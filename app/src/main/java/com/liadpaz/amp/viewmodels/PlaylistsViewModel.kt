package com.liadpaz.amp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.liadpaz.amp.model.repositories.MainRepository
import com.liadpaz.amp.model.repositories.SongsRepository
import com.liadpaz.amp.view.data.Song
import java.util.ArrayDeque

class PlaylistsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SongsRepository.getInstance(application)

    val playlistsObservable = repository.getPlaylists()
    val recentlyAddedPlaylist = repository.getRecentlyAddedPlaylist()

    fun addPlaylist(playlistName: String, songs: ArrayList<Song>?) =
            repository.addPlaylist(playlistName, songs)

    fun deletePlaylist(playlistName: String) =
            repository.deletePlaylist(playlistName)

    fun renamePlaylist(prevName: String, newName: String) =
            repository.renamePlaylist(prevName, newName)

    fun addSongToPlaylist(playlistName: String, song: Song?, songs: List<Song>?) {
        repository.addSongToPlaylist(playlistName, song, songs)
    }
}