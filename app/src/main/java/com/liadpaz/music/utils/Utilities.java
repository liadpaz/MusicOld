package com.liadpaz.music.utils;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;

public class Utilities {

    @SuppressWarnings("usused")
    private static final String TAG = "UTILITIES";

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
    public static ArrayList<File> listFiles(String path) {
        ArrayList<File> files = new ArrayList<>();
        try {
            for (File file : new File(path).listFiles()) {
                if (!file.isDirectory()) {
                    String fileName = file.getName();
                    if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".m4a") || fileName.endsWith(".mwa") || fileName.endsWith(".flac")) {
                        files.add(file);
                    }
                } else {
                    files.addAll(listFiles(file.getPath()));
                }
            }
            return files;
        } catch (Exception ignored) {
            return files;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static String getPathFromUri(@NonNull Uri uri) {
        String[] path = uri.getPath().split(":")[0].split("/");
        StringBuilder builder = new StringBuilder("/storage/self/primary");
        for (int i = 3; i < path.length; i++) {
            builder.append("/").append(path[i]);
        }
        builder.append("/").append(uri.getPath().split(":")[1].split("/")[0]);
        return builder.toString();
    }
}
