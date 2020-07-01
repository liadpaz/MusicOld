package com.liadpaz.amp.livedatautils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

object ColorUtil {
    private val color = MutableLiveData(Color.BLACK)
    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<Int>) {
        color.observe(lifecycleOwner, observer)
    }

    fun setColor(@ColorInt color: Int) {
        ColorUtil.color.value = color
    }
}