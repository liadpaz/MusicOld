package com.liadpaz.amp.livedatautils

import androidx.lifecycle.MutableLiveData
import com.liadpaz.amp.viewmodels.Song
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

object QueueUtil {
    val queue = MutableLiveData(ArrayList<Song>())
    val queuePosition = MutableLiveData(-1)
    private val isChangingInternal = AtomicBoolean(false)

    const val TAG = "AmpApp.QueueUtil"

    var isChanging: Boolean
        get() = isChangingInternal.get()
        set(value) = isChangingInternal.set(value)

    val queueSize: Int
        get() = queue.value!!.size
}