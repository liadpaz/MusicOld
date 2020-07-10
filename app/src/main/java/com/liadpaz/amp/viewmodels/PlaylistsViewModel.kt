package com.liadpaz.amp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.liadpaz.amp.service.repository.Repository

class PlaylistsViewModel(application: Application) : AndroidViewModel(application) {
    val playlistsObservable = Repository.getInstance(application).getPlaylists()
    val recentlyAddedPlaylist = Repository.getInstance(application).getRecentlyAddedPlaylist()
}