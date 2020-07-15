package com.liadpaz.amp.view.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.liadpaz.amp.view.fragments.AlbumListFragment
import com.liadpaz.amp.view.fragments.ArtistListFragment
import com.liadpaz.amp.view.fragments.PlaylistsFragment
import com.liadpaz.amp.view.fragments.SongsListFragment

class MainViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment =
        when (position) {
            0 -> SongsListFragment.newInstance()
            1 -> PlaylistsFragment.newInstance()
            2 -> ArtistListFragment.newInstance()
            3 -> AlbumListFragment.newInstance()
            else -> throw IndexOutOfBoundsException("Max 4 pages!")
        }

    override fun getItemCount(): Int = 4
}