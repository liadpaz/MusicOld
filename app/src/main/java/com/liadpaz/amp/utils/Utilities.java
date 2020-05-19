package com.liadpaz.amp.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Utilities {
    @SuppressWarnings("usused")
    private static final String TAG = "UTILITIES";

    private static final Uri albumsUri = Uri.parse("content://media/external/audio/albumart");

    @NonNull
    public static String joinArtists(@NonNull ArrayList<String> artists) {
        StringBuilder joinedArtists = new StringBuilder(artists.get(0));
        for (int i = 1; i < artists.size(); i++) {
            joinedArtists.append(", ").append(artists.get(i));
        }
        return joinedArtists.toString();
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static String getPathFromUri(@NonNull Uri uri) {
        String[] path = uri.getPath().split(":")[0].split("/");
        StringBuilder builder = new StringBuilder();
        for (int i = 3; i < path.length; i++) {
            builder.append("/").append(path[i]);
        }
        builder.append("/").append(uri.getPath().split(":")[1].split("/")[0]);
        return builder.toString();
    }

    @NonNull
    public static Uri getCoverUri(@NonNull Song song) {
        return ContentUris.withAppendedId(albumsUri, Long.parseLong(song.getAlbumId()));
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    public static String formatTime(long millis) {
        long minutesTime = TimeUnit.MILLISECONDS.toMinutes(millis);
        long secondsTime = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        StringBuilder seconds = new StringBuilder(String.valueOf(secondsTime));
        if (secondsTime < 10) {
            seconds.insert(0, "0");
        }
        return String.format("%s:%s", String.valueOf(minutesTime), seconds);
    }

    @SuppressWarnings("ConstantConditions")
    public static Bitmap getBitmapFromVectorDrawable(@NonNull Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
