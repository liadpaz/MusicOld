package com.liadpaz.amp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentExtendedSongBinding
import com.liadpaz.amp.livedatautils.QueueUtil

class ExtendedSongFragment : Fragment() {

    private var position: Int = -1

    private lateinit var binding: FragmentExtendedSongBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        position = arguments?.getInt("position")!!
        return FragmentExtendedSongBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Glide.with(this).load(QueueUtil.queue.value!![position].artUri).placeholder(R.drawable.song).into(binding.ivSongCover)
    }

    companion object {
        private const val TAG = "AmpApp.ExtendedSongFragment"

        @JvmStatic
        fun newInstance(position: Int): ExtendedSongFragment {
            return ExtendedSongFragment().apply {
                arguments = bundleOf(Pair("position", position))
            }
        }
    }

}