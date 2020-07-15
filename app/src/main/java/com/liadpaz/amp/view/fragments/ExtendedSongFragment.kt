package com.liadpaz.amp.view.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.liadpaz.amp.databinding.FragmentExtendedSongBinding
import com.liadpaz.amp.utils.GlideApp

class ExtendedSongFragment : Fragment() {

    private lateinit var uri: Uri

    private lateinit var binding: FragmentExtendedSongBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        uri = arguments?.getParcelable("uri")!!
        return FragmentExtendedSongBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        GlideApp.with(this).load(uri).into(binding.ivSongCover)
    }

    companion object {
        @JvmStatic
        fun newInstance(uri: Uri?): ExtendedSongFragment {
            return ExtendedSongFragment().apply {
                arguments = bundleOf(Pair("uri", uri ?: Uri.EMPTY))
            }
        }
    }
}

private const val TAG = "AmpApp.ExtendedSongFragment"