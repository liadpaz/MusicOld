package com.liadpaz.amp.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.liadpaz.amp.R;
import com.liadpaz.amp.service.MediaPlayerService;
import com.liadpaz.amp.utils.Constants;

public final class MediaNotification {
    private static final String TAG = "AmpApp.MediaNotification";

    private static final String CHANNEL_ID = "music_channel";

    public MediaNotification(@NonNull Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_name), NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static Notification.Builder from(@NonNull final Context context, @NonNull final MediaSession mediaSession) {
        MediaController controller = mediaSession.getController();
        MediaMetadata metadata = controller.getMetadata();
        try {
            MediaDescription description = metadata.getDescription();

            final boolean isPlaying = controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING;
            final boolean isLooping = controller.getPlaybackState().getExtras().getBoolean(Constants.LOOP_EXTRA);

            PendingIntent repeatIntent = PendingIntent.getService(context, 1, new Intent(context, MediaPlayerService.class).putExtra(Constants.LOOP_EXTRA, !isLooping), PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent skipToPrevIntent = PendingIntent.getService(context, 2, new Intent(context, MediaPlayerService.class).putExtra(Constants.ACTION, Constants.ACTION_SKIP_PREV), PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent playPauseIntent = PendingIntent.getService(context, 3, new Intent(context, MediaPlayerService.class).putExtra(Constants.ACTION, isPlaying ? Constants.ACTION_PAUSE : Constants.ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent skipToNextIntent = PendingIntent.getService(context, 4, new Intent(context, MediaPlayerService.class).putExtra(Constants.ACTION, Constants.ACTION_SKIP_NEXT), PendingIntent.FLAG_UPDATE_CURRENT);

            return new Notification.Builder(context, CHANNEL_ID).setContentIntent(controller.getSessionActivity())
                                                                .setContentTitle(description.getTitle())
                                                                .setContentText(description.getSubtitle())
                                                                .setSubText(description.getDescription())
                                                                .setContentIntent(controller.getSessionActivity())
                                                                .setVisibility(Notification.VISIBILITY_PUBLIC)
                                                                .setShowWhen(false)
                                                                .addAction(new Notification.Action.Builder(Icon.createWithResource(context, isLooping ? R.drawable.repeat_one : R.drawable.repeat), null, repeatIntent).build())
                                                                .addAction(new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.skip_prev), null, skipToPrevIntent).build())
                                                                .addAction(new Notification.Action.Builder(Icon.createWithResource(context, isPlaying ? R.drawable.pause : R.drawable.play), null, playPauseIntent).build())
                                                                .addAction(new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.skip_next), null, skipToNextIntent).build())
                                                                .setStyle(new Notification.MediaStyle().setShowActionsInCompactView(1, 2, 3).setMediaSession(mediaSession.getSessionToken()))
                                                                .setSmallIcon(R.drawable.ic_launcher_foreground);
        } catch (Exception ignored) {
            return null;
        }
    }
}
