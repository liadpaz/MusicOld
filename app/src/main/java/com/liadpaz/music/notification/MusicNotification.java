package com.liadpaz.music.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.liadpaz.music.ActionReceiver;
import com.liadpaz.music.MainActivity;
import com.liadpaz.music.R;
import com.liadpaz.music.utils.Constants;
import com.liadpaz.music.utils.Song;
import com.liadpaz.music.utils.Utilities;

public final class MusicNotification {

    private static final String CHANNEL_ID = "music_channel";

    //    private static final String TAG = "";
    private static final int NOTIFICATION_ID = 273;

    private static final int REQUEST_CODE = 869;
    private static final String TAG = "NOTIFICATION";

    private Context context;

    private NotificationManagerCompat notificationManager;

    public MusicNotification(@NonNull Context context) {
        notificationManager = NotificationManagerCompat.from(context);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_name), NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);

        this.context = context.getApplicationContext();
    }

    public void sendNotification(@NonNull Song song, boolean isPlaying) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        RemoteViews remoteViewsBig = new RemoteViews(context.getPackageName(), R.layout.notification_big);

        // set song name
        try {
            remoteViews.setTextViewText(R.id.tvSmallSongName, song.getSongName());
            remoteViewsBig.setTextViewText(R.id.tvBigSongName, song.getSongName());
        } catch (Exception e) {
            remoteViews.setTextViewText(R.id.tvSmallSongName, song.getPath()
                                                                  .substring(song.getPath()
                                                                                 .lastIndexOf("/")));
            remoteViewsBig.setTextViewText(R.id.tvBigSongName, song.getPath()
                                                                   .substring(song.getPath()
                                                                                  .lastIndexOf("/")));
        }
        // set song preview
        try {
            remoteViews.setImageViewBitmap(R.id.ivSmallPreview, song.getCover());
            remoteViewsBig.setImageViewBitmap(R.id.ivBigPreview, song.getCover());
        } catch (Exception e) {
            remoteViews.setImageViewResource(R.id.ivSmallPreview, R.drawable.ic_audiotrack_black_24dp);
            remoteViewsBig.setImageViewResource(R.id.ivBigPreview, R.drawable.ic_audiotrack_black_24dp);
        }
        // set artist
        try {
            remoteViews.setTextViewText(R.id.tvSmallArtist, Utilities.joinArtists(song.getArtists()));
            remoteViewsBig.setTextViewText(R.id.tvBigArtist, Utilities.joinArtists(song.getArtists()));
        } catch (Exception e) {
            remoteViews.setTextViewText(R.id.tvSmallArtist, "No Artist");
            remoteViewsBig.setTextViewText(R.id.tvBigArtist, "No Artist");
        }
        // set play button
        remoteViews.setInt(R.id.ibSmallPlay, "setBackgroundResource", isPlaying ? R.drawable.pause : R.drawable.play);
        remoteViewsBig.setInt(R.id.ibBigPlay, "setBackgroundResource", isPlaying ? R.drawable.pause : R.drawable.play);

        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(context, 3, new Intent(context, ActionReceiver.class).putExtra(Constants.ACTION_EXTRA, Constants.ACTION_PREV_EXTRA), 0);

        PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 1, new Intent(context, ActionReceiver.class).putExtra(Constants.ACTION_EXTRA, Constants.ACTION_PLAY_EXTRA), 0);

        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 2, new Intent(context, ActionReceiver.class).putExtra(Constants.ACTION_EXTRA, Constants.ACTION_NEXT_EXTRA), 0);

        remoteViews.setOnClickPendingIntent(R.id.ibSmallPrev, prevPendingIntent);
        remoteViewsBig.setOnClickPendingIntent(R.id.ibBigPrev, prevPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.ibSmallPlay, playPendingIntent);
        remoteViewsBig.setOnClickPendingIntent(R.id.ibBigPlay, playPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.ibSmallNext, nextPendingIntent);
        remoteViewsBig.setOnClickPendingIntent(R.id.ibBigNext, nextPendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground)
                                                                                                .setContentIntent(PendingIntent.getActivity(context, REQUEST_CODE, new Intent(context, MainActivity.class), 0))
                                                                                                .setStyle(new androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle())
                                                                                                .setCustomContentView(remoteViews)
                                                                                                .setCustomBigContentView(remoteViewsBig)
                                                                                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                                                                                .setOngoing(isPlaying);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void stopNotification() {
        notificationManager.cancelAll();
    }
}
