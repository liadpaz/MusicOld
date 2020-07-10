package com.liadpaz.amp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.liadpaz.amp.service.repository.Repository

class QueueViewModel(application: Application) : AndroidViewModel(application) {
    val queueObservable = Repository.getInstance(application).getQueue()
    val positionObservable = Repository.getInstance(application).getPosition()
}