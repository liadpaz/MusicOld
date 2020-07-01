package com.liadpaz.amp.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.liadpaz.amp.MainActivity
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentControllerBinding
import com.liadpaz.amp.livedatautils.ColorUtil
import com.liadpaz.amp.service.ServiceConnector.Companion.getInstance
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.viewmodels.CurrentSong

class ControllerFragment : Fragment() {
    private val TAG = "AmpApp.ControllerFragment"

    private var isPlaying = false
    private var isBright = false

    private lateinit var transportControls: MediaControllerCompat.TransportControls

    private lateinit var binding: FragmentControllerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentControllerBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val serviceConnector = getInstance()!!
        serviceConnector.playbackState.observe(viewLifecycleOwner, Observer { state: PlaybackStateCompat -> setPlayback(state) })
        serviceConnector.nowPlaying.observe(viewLifecycleOwner, Observer { metadata: MediaMetadataCompat -> setMetadata(metadata) })
        transportControls = serviceConnector.transportControls!!
        binding.btnPlay.setOnClickListener {
            if (isPlaying) {
                transportControls.pause()
            } else {
                transportControls.play()
            }
        }
        ColorUtil.observe(this, Observer { color: Int? ->
            if (Utilities.isColorBright(color!!).also { isBright = it }) {
                binding.tvSongArtist.setTextColor(Color.BLACK)
                binding.tvSongTitle.setTextColor(Color.BLACK)
                binding.btnPlay.setImageResource(if (isPlaying) R.drawable.pause_pressed else R.drawable.play_pressed)
                binding.btnPlay.imageTintList = ColorStateList.valueOf(Color.BLACK)
            } else {
                val typedValue = TypedValue()
                val theme = requireActivity().theme
                theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
                val arr = requireActivity().obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.textColorPrimary))
                val primaryColor = arr.getColor(0, -1)
                arr.recycle()
                binding.tvSongArtist.setTextColor(primaryColor)
                binding.tvSongTitle.setTextColor(primaryColor)
                binding.btnPlay.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.play)
                binding.btnPlay.imageTintList = ColorStateList.valueOf(Color.WHITE)
            }
        })
        binding.root.setOnClickListener {
            BottomSheetBehavior.from((requireActivity() as MainActivity).binding.extendedFragment)
                    .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }

    private fun setPlayback(state: PlaybackStateCompat) {
        isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING
        binding.btnPlay.setImageResource(if (state.state == PlaybackStateCompat.STATE_PLAYING) R.drawable.pause else R.drawable.play)
        binding.btnPlay.imageTintList = ColorStateList.valueOf(if (isBright) Color.BLACK else Color.WHITE)
    }

    private fun setMetadata(metadata: MediaMetadataCompat) {
        val description = metadata.description
        if (description != null) {
            try {
                Glide.with(this).load(description.iconUri).placeholder(R.drawable.song).into(binding.ivCurrentTrack)
                binding.song = CurrentSong(description.title.toString(), description.subtitle.toString())
            } catch (ignored: Exception) {
            }
        }
    }

    companion object {
        fun newInstance(): ControllerFragment {
            return ControllerFragment()
        }
    }
}