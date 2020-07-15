package com.liadpaz.amp.view.views

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.liadpaz.amp.R
import com.liadpaz.amp.view.data.Song
import com.liadpaz.amp.view.dialogs.PlaylistsDialog
import com.liadpaz.amp.viewmodels.SongListViewModel

class SongPopUpMenu(private val fragment: Fragment, view: View, private val song: Song) : PopupMenu(fragment.requireContext(), view) {

    val viewModel: SongListViewModel by fragment.viewModels()

    init {
        inflate(R.menu.menu_song)
        setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuPlayNext -> {
                    viewModel
                }
                R.id.menuAddQueue -> {
                    viewModel
                }
                R.id.menuAddToPlaylist -> {
                    PlaylistsDialog.newInstance(song).show(fragment.childFragmentManager, null)
                }
            }
            true
        }
    }
}