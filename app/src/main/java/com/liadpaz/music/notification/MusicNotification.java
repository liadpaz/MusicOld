package com.liadpaz.music.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.liadpaz.music.R;
import com.liadpaz.music.service.MusicPlayerService;
import com.liadpaz.music.utils.Constants;
import com.liadpaz.music.utils.HelperActivity;

import java.lang.ref.WeakReference;

public final class MusicNotification extends Notification {

    private static final String CHANNEL_ID = "music_channel";

//    private static final String TAG = "";
    private static final int NOTIFICATION_ID = 273;

    private static final int REQUEST_CODE = 869;

    private WeakReference<Context> contextWeakReference;

    private NotificationManagerCompat notificationManager;

    public MusicNotification(@NonNull Context context, @NonNull NotificationManager notificationManager) {
        this.notificationManager = NotificationManagerCompat.from(context);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_name), NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        contextWeakReference = new WeakReference<>(context);
    }

    public void sendNotification() {
        RemoteViews remoteViews = new RemoteViews(contextWeakReference.get().getPackageName(), R.layout.notification_small);

        PendingIntent playPendingIntent = PendingIntent.getService(contextWeakReference.get(), 1, new Intent(contextWeakReference.get(), HelperActivity.class).putExtra(Constants.ACTION_EXTRA, Constants.ACTION_PLAY_EXTRA),0);
        remoteViews.setOnClickPendingIntent(R.id.btnSmallPlay, playPendingIntent);

        PendingIntent nextPendingIntent = PendingIntent.getService(contextWeakReference.get(), 2, new Intent(contextWeakReference.get(), HelperActivity.class).putExtra(Constants.ACTION_EXTRA, Constants.ACTION_NEXT_EXTRA),0);
        remoteViews.setOnClickPendingIntent(R.id.btnSmallNext, nextPendingIntent);

        PendingIntent prevPendingIntent = PendingIntent.getService(contextWeakReference.get(), 3, new Intent(contextWeakReference.get(), HelperActivity.class).putExtra(Constants.ACTION_EXTRA, Constants.ACTION_PREV_EXTRA),0);
        remoteViews.setOnClickPendingIntent(R.id.btnSmallPrev, prevPendingIntent);

        Notification notification = new NotificationCompat.Builder(contextWeakReference.get(), CHANNEL_ID)
                                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                                            .setContentIntent(PendingIntent.getActivity(contextWeakReference.get(), REQUEST_CODE, new Intent(contextWeakReference.get(), MusicPlayerService.class), 0))
                                            .setContentTitle("Music")
                                            .setContentText("Text")
                                            .setCategory(NotificationCompat.CATEGORY_SERVICE)
                                            .setCustomContentView(remoteViews)
                                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                            .build();
        notification.flags |= NotificationCompat.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
