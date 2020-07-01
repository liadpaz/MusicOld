package com.liadpaz.amp.fragments

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.liadpaz.amp.adapters.ExtendedViewPagerAdapter
import com.liadpaz.amp.databinding.FragmentExtendedViewPagerBinding
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.service.MediaPlayerService
import com.liadpaz.amp.service.ServiceConnector.Companion.getInstance
import com.liadpaz.amp.viewmodels.Song
import java.util.*

class ExtendedViewPagerFragment private constructor() : Fragment() {
    private val TAG = "AmpApp.ExtendedViewPagerFragment"

    private var isCreated = false

    private lateinit var adapter: ExtendedViewPagerAdapter
    private lateinit var callback: OnPageChangeCallback

    private lateinit var binding: FragmentExtendedViewPagerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentExtendedViewPagerBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ExtendedViewPagerAdapter(this)
        binding.extendedViewPager.adapter = adapter
        QueueUtil.queuePosition.observe(viewLifecycleOwner, Observer { position: Int? ->
            if (!isCreated) {
                isCreated = true
                binding.extendedViewPager.setCurrentItem(position!!, false)
            } else {
                binding.extendedViewPager.currentItem = position!!
            }
        })
        QueueUtil.queue.observe(viewLifecycleOwner, Observer { binding.extendedViewPager.adapter = ExtendedViewPagerAdapter(this).also { adapter = it } })
        binding.extendedViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            private var firstTime = true
            override fun onPageSelected(position: Int) {
                if (firstTime) {
                    firstTime = false
                } else if (binding.extendedViewPager.currentItem != QueueUtil.queuePosition.value) {
                    getInstance(requireContext(), ComponentName(requireContext(), MediaPlayerService::class.java))!!.transportControls!!.skipToQueueItem(position.toLong())
                }
            }
        }.also { callback = it })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.extendedViewPager.unregisterOnPageChangeCallback(callback)
    }

    companion object {
        fun newInstance(): ExtendedViewPagerFragment {
            return ExtendedViewPagerFragment()
        }
    }
}