package com.liadpaz.amp.view.fragments

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.liadpaz.amp.databinding.FragmentExtendedViewPagerBinding
import com.liadpaz.amp.server.service.MediaPlayerService
import com.liadpaz.amp.server.service.ServiceConnection.Companion.getInstance
import com.liadpaz.amp.view.adapters.ExtendedViewPagerAdapter
import com.liadpaz.amp.viewmodels.QueueViewModel

class ExtendedViewPagerFragment : Fragment() {

    private var isCreated = false

    private lateinit var adapter: ExtendedViewPagerAdapter

    private val viewModel: QueueViewModel by viewModels()
    private lateinit var binding: FragmentExtendedViewPagerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentExtendedViewPagerBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ExtendedViewPagerAdapter(this).also { binding.extendedViewPager.adapter = it }
        viewModel.getPosition().observe(viewLifecycleOwner) { position ->
            if (!isCreated) {
                isCreated = true
                binding.extendedViewPager.setCurrentItem(position, false)
            } else {
                binding.extendedViewPager.currentItem = position
            }
        }
        viewModel.getQueue().observe(viewLifecycleOwner) { binding.extendedViewPager.adapter = ExtendedViewPagerAdapter(this).also { adapter = it } }
        binding.extendedViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            private var firstTime = true

            override fun onPageSelected(position: Int) {
                if (firstTime) {
                    firstTime = false
                } else if (binding.extendedViewPager.currentItem != viewModel.getPosition().value) {
                    getInstance(requireContext(), ComponentName(requireContext(), MediaPlayerService::class.java)).transportControls.skipToQueueItem(position.toLong())
                }
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(): ExtendedViewPagerFragment =
                ExtendedViewPagerFragment()
    }
}

private const val TAG = "AmpApp.ExtendedSwipe"