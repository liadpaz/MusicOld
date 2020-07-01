package com.liadpaz.amp.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.liadpaz.amp.R;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public final class AmpNotificationManager {
    private static final String TAG = "AmpApp.AmpNotificationManager";

    private static final String CHANNEL_ID = "music_channel";

    private static final int NOTIFICATION_ID = 273;

    private final Context context;
    private final ExoPlayer player;

    private final PlayerNotificationManager notificationManager;

    @SuppressWarnings("ConstantConditions")
    public AmpNotificationManager(@NonNull Context context, @NonNull ExoPlayer player, @NonNull final MediaSessionCompat.Token sessionToken, @NonNull PlayerNotificationManager.NotificationListener notificationListener) {
        this.context = context;
        this.player = player;

        if (Build.VERSION.SDK_INT >= 26) {
            ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(new NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_name), NotificationManager.IMPORTANCE_LOW));
        }

        MediaControllerCompat mediaController = null;
        try {
            mediaController = new MediaControllerCompat(context, sessionToken);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        notificationManager = new PlayerNotificationManager(context, CHANNEL_ID, NOTIFICATION_ID, new DescriptionAdapter(mediaController), notificationListener) {
            {
                setMediaSessionToken(sessionToken);
                setSmallIcon(R.drawable.ic_launcher_foreground);
                setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);

                setUseChronometer(false);

                setUseNavigationActionsInCompactView(true);

                setRewindIncrementMs(0);
                setFastForwardIncrementMs(0);
            }
        };
    }

    public void showNotification() {
        notificationManager.setPlayer(player);
    }

    public void hideNotification() {
        notificationManager.setPlayer(null);
    }

    private class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {
        private static final String TAG = "AmpApp.DescriptionAdapter";

        private final MediaControllerCompat controller;

        private Uri currentIconUri = null;
        private volatile Bitmap currentBitmap = null;

        public DescriptionAdapter(@NonNull MediaControllerCompat controller) {
            this.controller = controller;
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            return controller.getSessionActivity();
        }

        @Nullable
        @Override
        public String getCurrentSubText(Player player) {
            CharSequence description = controller.getMetadata().getDescription().getDescription();
            if (description == null) {
                return null;
            }
            return description.toString();
        }

        @Nullable
        @Override
        public String getCurrentContentText(Player player) {
            CharSequence subtitle = controller.getMetadata().getDescription().getSubtitle();
            if (subtitle == null) {
                return null;
            }
            return subtitle.toString();
        }

        @Override
        public String getCurrentContentTitle(Player player) {
            CharSequence title = controller.getMetadata().getDescription().getTitle();
            if (title == null) {
                return null;
            }
            return title.toString();
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            Uri iconUri = controller.getMetadata().getDescription().getIconUri();
            if (currentIconUri != iconUri || currentBitmap == null) {
                currentIconUri = iconUri;
                CompletableFuture.runAsync(() -> {
                    currentBitmap = resolveUriAsBitmap(iconUri);
                    callback.onBitmap(currentBitmap);
                });
                return currentBitmap;
            }
            return currentBitmap;
        }

        @SuppressWarnings("ConstantConditions")
        @Nullable
        private Bitmap resolveUriAsBitmap(@Nullable Uri uri) {
            try (InputStream fileStream = context.getContentResolver().openInputStream(uri)) {
                return BitmapFactory.decodeStream(fileStream);
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
