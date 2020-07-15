package com.liadpaz.amp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.liadpaz.amp.model.repositories.MainRepository
import com.liadpaz.amp.model.repositories.SongsRepository

class AlbumListViewModel(application: Application) : AndroidViewModel(application) {
    val albums = SongsRepository.getInstance(application).getAlbums()
}