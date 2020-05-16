package com.liadpaz.amp.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.session.MediaButtonReceiver;

import com.liadpaz.amp.R;

public final class MediaNotification {

    private static final String CHANNEL_ID = "music_channel";

    public MediaNotification(@NonNull Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_name), NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }

    public static NotificationCompat.Builder from(@NonNull Context context, @NonNull MediaSessionCompat mediaSession, boolean isPlaying) {
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat metadata = controller.getMetadata();
        MediaDescriptionCompat description = metadata.getDescription();

        return new NotificationCompat.Builder(context, CHANNEL_ID).setContentIntent(controller.getSessionActivity())
                                                                  .setContentTitle(description.getTitle())
                                                                  .setContentText(description.getSubtitle())
                                                                  .setSubText(description.getDescription())
                                                                  .setLargeIcon(description.getIconBitmap())
                                                                  .setContentIntent(controller.getSessionActivity())
                                                                  .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                                                  .setShowWhen(false)
                                                                  .addAction(new NotificationCompat.Action(R.drawable.skip_prev,null, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)))
                                                                  .addAction(new NotificationCompat.Action(isPlaying ? R.drawable.pause : R.drawable.play,null, MediaButtonReceiver.buildMediaButtonPendingIntent(context, isPlaying ?  PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY)))
                                                                  .addAction(new NotificationCompat.Action(R.drawable.skip_next,null, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)))
                                                                  .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                                                                    .setShowActionsInCompactView(0, 1, 2)
                                                                                    .setMediaSession(mediaSession.getSessionToken())
                                                                                    .setShowCancelButton(true)
                                                                                    .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)))
                                                                  .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
                                                                  .setSmallIcon(R.drawable.ic_launcher_foreground);
    }
}
