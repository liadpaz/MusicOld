package com.liadpaz.amp.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.liadpaz.amp.viewmodels.Song;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {
    @SuppressWarnings("unused")
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
        String[] path = uri.getPath().split(":");
        return path.length == 1 ? "" : path[1];
    }

    public static boolean isColorBright(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) > 0.5;
    }

    @NonNull
    public static Uri getCoverUri(@NonNull Song song) {
        return ContentUris.withAppendedId(albumsUri, Long.parseLong(song.albumId));
    }

    /**
     * This function returns the time in a formatted string, eg. 0 millis to 0:00.
     *
     * @param millis the time to format in milliseconds
     * @return the formatted
     */
    @NonNull
    @SuppressLint("DefaultLocale")
    public static String formatTime(long millis) {
        long minutesTime = TimeUnit.MILLISECONDS.toMinutes(millis);
        long secondsTime = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        StringBuilder seconds = new StringBuilder(String.valueOf(secondsTime));
        if (secondsTime < 10) {
            seconds.insert(0, "0");
        }
        return String.format("%s:%s", minutesTime, seconds);
    }

    @Nullable
    public static InputStream getInputStream(@NonNull Context context, @NonNull Song song) {
        try {
            return context.getContentResolver().openInputStream(getCoverUri(song));
        } catch (Exception ignored) {
            return null;
        }
    }

    @NonNull
    public static ArrayList<String> getArtistsFromSong(@NonNull String title, @NonNull String artistsString) {
        ArrayList<String> artists = new ArrayList<>();
        Matcher matcher = Pattern.compile("([^ &,]([^,&])*[^ ,&]+)").matcher(artistsString);
        while (matcher.find()) {
            artists.add(matcher.group());
        }

        //        int openBracketIndex;
        //        int closeBracketIndex;
        //        if ((openBracketIndex = title.indexOf('(')) != -1 && (closeBracketIndex = title.indexOf(')')) != -1 && openBracketIndex < closeBracketIndex) {
        //
        //        }
        return artists;
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
