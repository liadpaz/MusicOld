package com.liadpaz.amp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentExtendedSongBinding
import com.liadpaz.amp.livedatautils.QueueUtil

class ExtendedSongFragment private constructor(private val position: Int) : Fragment() {
    private val TAG = "AmpApp.ExtendedSongFragment"

    private lateinit var binding: FragmentExtendedSongBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentExtendedSongBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Glide.with(this).load(QueueUtil.queue.value!![position].coverUri).placeholder(R.drawable.song).into(binding.ivSongCover)
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): ExtendedSongFragment {
            return ExtendedSongFragment(position)
        }
    }

}