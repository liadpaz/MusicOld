package com.liadpaz.amp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import com.liadpaz.amp.R
import com.liadpaz.amp.adapters.MainViewPagerAdapter
import com.liadpaz.amp.databinding.FragmentMainViewPagerBinding
import java.util.*

class MainViewPagerFragment : Fragment() {
    private lateinit var binding: FragmentMainViewPagerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentMainViewPagerBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewPager = binding.viewPagerMain
        viewPager.adapter = MainViewPagerAdapter(childFragmentManager, lifecycle)
        val tabsTitle: ArrayList<String> = ArrayList<String>().apply {
            add(getString(R.string.tab_songs))
            add(getString(R.string.tab_playlists))
            add(getString(R.string.tab_artists))
            add(getString(R.string.tab_albums))
        }
        TabLayoutMediator(binding.tabLayoutMain, viewPager, TabConfigurationStrategy { tab: TabLayout.Tab, position: Int -> tab.text = tabsTitle[position] }).attach()
    }

    companion object {
        @JvmStatic
        fun newInstance(): MainViewPagerFragment {
            return MainViewPagerFragment()
        }
    }
}