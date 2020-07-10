package com.liadpaz.amp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.liadpaz.amp.service.repository.Repository

class ArtistListViewModel(application: Application) : AndroidViewModel(application) {
    val artistsObservable = Repository.getInstance(application).getArtists()
}