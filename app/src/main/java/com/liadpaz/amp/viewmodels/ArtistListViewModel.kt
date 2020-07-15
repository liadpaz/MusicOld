package com.liadpaz.amp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.liadpaz.amp.model.repositories.MainRepository
import com.liadpaz.amp.model.repositories.SongsRepository

class ArtistListViewModel(application: Application) : AndroidViewModel(application) {
    val artists = SongsRepository.getInstance(application).getArtists()
}