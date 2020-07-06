package com.liadpaz.amp.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.liadpaz.amp.ui.fragments.ExtendedSongFragment.Companion.newInstance
import com.liadpaz.amp.livedatautils.QueueUtil

class ExtendedViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val TAG = "AmpApp.ExtendedViewPagerAdapter"

    override fun createFragment(position: Int): Fragment {
        return newInstance(position)
    }

    override fun getItemCount(): Int = QueueUtil.queueSize
}