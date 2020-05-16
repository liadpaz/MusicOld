package com.liadpaz.amp.utils;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

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
    public static Uri getCover(@NonNull Song song) {
        return ContentUris.withAppendedId(albumsUri, Long.parseLong(song.getAlbumId()));
    }

    public static CompletableFuture<Bitmap> isUriExists(@NonNull Context context, @NonNull Uri uri) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
            } catch (Exception ignored) {
                return null;
            }
        });
    }
}
