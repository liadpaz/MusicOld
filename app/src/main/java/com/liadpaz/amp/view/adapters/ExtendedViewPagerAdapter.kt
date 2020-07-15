package com.liadpaz.amp.view.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.liadpaz.amp.view.fragments.ExtendedSongFragment
import com.liadpaz.amp.viewmodels.QueueViewModel

class ExtendedViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val viewModel: QueueViewModel by fragment.viewModels()

    override fun getItemCount(): Int = viewModel.getQueue().value?.size ?: 0

    override fun createFragment(position: Int): Fragment =
            ExtendedSongFragment.newInstance(viewModel.getQueue().value?.get(position)?.artUri)
}

private const val TAG = "AmpApp.ExtendedViewPagerAdapter"