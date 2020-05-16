package com.liadpaz.amp.utils;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

public class MinutesSeconds {
    @NonNull
    @SuppressLint("DefaultLocale")
    public static String format(long millis) {
        long minutesTime = TimeUnit.MILLISECONDS.toMinutes(millis);
        long secondsTime = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        String minutes = String.valueOf(minutesTime);
        String seconds = String.valueOf(secondsTime);
        if (secondsTime < 10) {
            seconds = "0" + seconds;
        }
        return String.format("%s:%s", minutes, seconds);
    }
}
