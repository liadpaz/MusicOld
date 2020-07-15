package com.liadpaz.amp.server.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.liadpaz.amp.R
import com.liadpaz.amp.utils.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AmpNotificationManager(
        private val context: Context,
        private val player: ExoPlayer,
        sessionToken: MediaSessionCompat.Token,
        notificationListener: PlayerNotificationManager.NotificationListener
) {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val notificationManager: PlayerNotificationManager

    private var defaultCover: Bitmap? = null

    init {
        if (Build.VERSION.SDK_INT >= 26) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_name), NotificationManager.IMPORTANCE_LOW))
        }

        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager = PlayerNotificationManager(context, CHANNEL_ID, NOTIFICATION_ID, DescriptionAdapter(mediaController), notificationListener).apply {
            setMediaSessionToken(sessionToken)
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)

            setUseChronometer(false)
            setUseNavigationActionsInCompactView(true)

            setRewindIncrementMs(0)
            setFastForwardIncrementMs(0)
        }

        serviceScope.launch {
            defaultCover = Utilities.getSongBitmap(context)
        }
    }

    fun showNotification() {
        notificationManager.setPlayer(player)
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    private inner class DescriptionAdapter(private val controller: MediaControllerCompat) : MediaDescriptionAdapter {
        private val TAG = "AmpApp.DescriptionAdapter"

        private var currentIconUri: Uri? = null
        private var currentBitmap: Bitmap? = null

        override fun createCurrentContentIntent(player: Player): PendingIntent? = controller.sessionActivity

        override fun getCurrentSubText(player: Player): String? =
                controller.metadata.description.description.toString()

        override fun getCurrentContentText(player: Player): String? =
                controller.metadata.description.subtitle.toString()

        override fun getCurrentContentTitle(player: Player): String =
                controller.metadata.description.title.toString()

        override fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap? {
            val iconUri = controller.metadata.description.iconUri

            return if (currentIconUri != iconUri || currentBitmap == null) {
                currentIconUri = iconUri
                serviceScope.launch {
                    resolveUriAsBitmap(iconUri)?.also {
                        currentBitmap = it
                        callback.onBitmap(it)
                    }
                }
                defaultCover
            } else currentBitmap
        }

        private fun resolveUriAsBitmap(uri: Uri?): Bitmap? =
                try {
                    uri?.let { context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) } }
                } catch (ignored: Exception) {
                    null
                }
    }

    companion object {
        private const val CHANNEL_ID = "music_channel"
        private const val NOTIFICATION_ID = 273
    }
}

private const val TAG = "AmpApp.NotificationManager"