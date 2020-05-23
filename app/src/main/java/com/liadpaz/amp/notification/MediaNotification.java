package com.liadpaz.amp.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.session.MediaButtonReceiver;

import com.liadpaz.amp.R;
import com.liadpaz.amp.service.MediaPlayerService;
import com.liadpaz.amp.utils.Constants;

public final class MediaNotification {

    private static final String CHANNEL_ID = "music_channel";
    private static final String TAG = "MEDIA_NOTIFICATION";

    public MediaNotification(@NonNull Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_name), NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }

    public static NotificationCompat.Builder from(@NonNull final Context context, @NonNull final MediaSessionCompat mediaSession) {
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat metadata = controller.getMetadata();
        MediaDescriptionCompat description = metadata.getDescription();

        final boolean isPlaying = controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;
        final boolean isLooping = controller.getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ONE;

        Log.d(TAG, "from: " + isLooping);

        PendingIntent repeatIntent = PendingIntent.getService(context, 4, new Intent(context, MediaPlayerService.class).putExtra(Constants.LOOP_EXTRA, 0), 0);

        return new NotificationCompat.Builder(context, CHANNEL_ID).setContentIntent(controller.getSessionActivity())
                                                                  .setContentTitle(description.getTitle())
                                                                  .setContentText(description.getSubtitle())
                                                                  .setSubText(description.getDescription())
                                                                  .setContentIntent(controller.getSessionActivity())
                                                                  .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                                                  .setShowWhen(false)
                                                                  .addAction(new NotificationCompat.Action(isLooping ? R.drawable.repeat_one : R.drawable.repeat, null, repeatIntent))
                                                                  .addAction(new NotificationCompat.Action(R.drawable.skip_prev, null, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)))
                                                                  .addAction(new NotificationCompat.Action(isPlaying ? R.drawable.pause : R.drawable.play, null, MediaButtonReceiver.buildMediaButtonPendingIntent(context, isPlaying ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY)))
                                                                  .addAction(new NotificationCompat.Action(R.drawable.skip_next, null, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)))
                                                                  .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                                                                    .setShowActionsInCompactView(1, 2, 3)
                                                                                    .setMediaSession(mediaSession.getSessionToken())
                                                                                    .setShowCancelButton(true)
                                                                                    .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)))
                                                                  .setSmallIcon(R.drawable.ic_launcher_foreground);
    }
}
