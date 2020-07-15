package com.liadpaz.amp.server.utils

import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource

/**
 * This class is almost a copy of [TimelineQueueEditor] with an edition of clear queue
 */
class QueueEditor(private val mediaController: MediaControllerCompat, private val queueMediaSource: ConcatenatingMediaSource, private val queueDataAdapter: QueueDataAdapter, private val sourceFactory: MediaSourceFactory) : MediaSessionConnector.QueueEditor, MediaSessionConnector.CommandReceiver {

    interface MediaSourceFactory {
        fun createMediaSource(description: MediaDescriptionCompat): MediaSource?
    }

    interface QueueDataAdapter {
        fun add(position: Int, mediaSource: MediaSource)
        fun remove(position: Int)
        fun move(from: Int, to: Int)
        fun clear()
    }

    override fun onAddQueueItem(player: Player, description: MediaDescriptionCompat) {
        onAddQueueItem(player, description, player.currentTimeline.windowCount)
    }

    override fun onAddQueueItem(player: Player, description: MediaDescriptionCompat, index: Int) {
        sourceFactory.createMediaSource(description)?.let {
            queueDataAdapter.add(index, it)
            queueMediaSource.addMediaSource(index, it)
        }
    }

    override fun onRemoveQueueItem(player: Player, description: MediaDescriptionCompat) {
        val queue = mediaController.queue
        for (i in 0 until queue.size) {
            if (queue[i].description.mediaId == description.mediaId) {
                queueDataAdapter.remove(i)
                queueMediaSource.removeMediaSource(i)
                return
            }
        }
    }

    override fun onCommand(player: Player, controlDispatcher: ControlDispatcher, command: String, extras: Bundle?, cb: ResultReceiver?): Boolean {
        when (command) {
            COMMAND_MOVE_QUEUE_ITEM -> {
                if (extras == null) return false
                val from = extras.getInt(EXTRA_FROM_INDEX)
                val to = extras.getInt(EXTRA_TO_INDEX)
                if (from != C.INDEX_UNSET && to != C.INDEX_UNSET) {
                    queueDataAdapter.move(from, to)
                    queueMediaSource.moveMediaSource(from, to)
                }
            }
            COMMAND_CLEAR_QUEUE -> {
                queueDataAdapter.clear()
                queueMediaSource.clear()
            }
            else -> return false
        }
        return true
    }
}

const val COMMAND_CLEAR_QUEUE = "clear"
const val COMMAND_MOVE_QUEUE_ITEM = "move_window"
const val EXTRA_FROM_INDEX = "from_index"
const val EXTRA_TO_INDEX = "to_index"