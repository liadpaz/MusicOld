package com.liadpaz.amp.view.fragments

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.liadpaz.amp.databinding.FragmentExtendedViewPagerBinding
import com.liadpaz.amp.viewmodels.livedatautils.QueueUtil
import com.liadpaz.amp.service.server.service.MediaPlayerService
import com.liadpaz.amp.service.server.service.ServiceConnection.Companion.getInstance
import com.liadpaz.amp.view.adapters.ExtendedViewPagerAdapter

class ExtendedViewPagerFragment : Fragment() {

    private var isCreated = false

    private lateinit var adapter: ExtendedViewPagerAdapter

    private lateinit var binding: FragmentExtendedViewPagerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentExtendedViewPagerBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ExtendedViewPagerAdapter(this).also { binding.extendedViewPager.adapter = it }
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
                    getInstance(requireContext(), ComponentName(requireContext(), MediaPlayerService::class.java)).transportControls.skipToQueueItem(position.toLong())
                }
            }
        })
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding.extendedViewPager.unregisterOnPageChangeCallback(callback)
//    }

    companion object {
        private const val TAG = "AmpApp.ExtendedSwipe"

        @JvmStatic
        fun newInstance(): ExtendedViewPagerFragment {
            return ExtendedViewPagerFragment()
        }
    }
}