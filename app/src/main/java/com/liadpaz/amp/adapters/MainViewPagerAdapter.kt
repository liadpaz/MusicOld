package com.liadpaz.amp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.liadpaz.amp.fragments.AlbumListFragment
import com.liadpaz.amp.fragments.ArtistListFragment
import com.liadpaz.amp.fragments.PlaylistsFragment
import com.liadpaz.amp.fragments.SongsListFragment.Companion.newInstance
import com.liadpaz.amp.livedatautils.SongsUtil

class MainViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int): Fragment =
        when (position) {
            0 -> newInstance(SongsUtil.getSongs())
            1 -> PlaylistsFragment.newInstance()
            2 -> ArtistListFragment.newInstance()
            3 -> AlbumListFragment.newInstance()
            else -> throw IndexOutOfBoundsException("Max 4 pages!")
        }

    override fun getItemCount(): Int = 4
}