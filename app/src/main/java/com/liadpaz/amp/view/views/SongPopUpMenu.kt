package com.liadpaz.amp.view.views

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.liadpaz.amp.R
import com.liadpaz.amp.service.server.service.ServiceConnection
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.view.dialogs.PlaylistsDialog
import com.liadpaz.amp.viewmodels.livedatautils.QueueUtil

class SongPopUpMenu(private val fragment: Fragment, view: View, private val song: Song) : PopupMenu(fragment.requireContext(), view) {
    init {
        inflate(R.menu.menu_song)
        setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuPlayNext -> {
                    ServiceConnection.getInstance().mediaSource.value?.addMediaSource(QueueUtil.queuePosition.value!! + 1, song.toMediaSource(fragment.requireContext()))
                }
                R.id.menuAddQueue -> {
                    ServiceConnection.getInstance().mediaSource.value?.addMediaSource(song.toMediaSource(fragment.requireContext()))
                }
                R.id.menuAddToPlaylist -> {
                    PlaylistsDialog(song).show(fragment.childFragmentManager, null)
                }
            }
            true
        }
    }
}