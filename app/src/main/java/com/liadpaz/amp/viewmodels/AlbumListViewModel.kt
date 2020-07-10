package com.liadpaz.amp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.liadpaz.amp.service.repository.Repository

class AlbumListViewModel(application: Application) : AndroidViewModel(application) {
    val albumsObservable = Repository.getInstance(application).getAlbums()
}