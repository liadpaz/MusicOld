package com.liadpaz.amp.utils;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class ColorUtil {
    private static MutableLiveData<Integer> color = new MutableLiveData<>(Color.BLACK);

    public static void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<Integer> observer) {
        color.observe(lifecycleOwner, observer);
    }

    public static void setColor(@ColorInt int color) {
        ColorUtil.color.postValue(color);
    }
}
