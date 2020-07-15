package com.liadpaz.amp.view.fragments

import android.content.ComponentName
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentControllerBinding
import com.liadpaz.amp.server.service.MediaPlayerService
import com.liadpaz.amp.server.service.ServiceConnection
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.view.MainActivity
import com.liadpaz.amp.view.data.CurrentSong
import com.liadpaz.amp.viewmodels.PlayingViewModel

class ControllerFragment : Fragment() {

    private var isPlaying = false
    private var isBright = false

    private var currentSong: CurrentSong? = null

    private val viewModel: PlayingViewModel by viewModels {
        val application = requireActivity().application
        PlayingViewModel.Factory(application, ServiceConnection.getInstance(application, ComponentName(application, MediaPlayerService::class.java)))
    }

    private lateinit var transportControls: MediaControllerCompat.TransportControls

    private lateinit var binding: FragmentControllerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentControllerBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        viewModel.getMediaMetadata().observe(viewLifecycleOwner) {
            if (currentSong != it) {
                currentSong = it
                binding.ivCurrentTrack.setImageBitmap(it.art)

                if (Utilities.isColorBright(it.color).also { isBright -> this.isBright = isBright }) {
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
            }
        }
        viewModel.getPlaybackState().observe(viewLifecycleOwner) {
            isPlaying = it.state == PlaybackStateCompat.STATE_PLAYING
            binding.btnPlay.setImageResource(if (it.state == PlaybackStateCompat.STATE_PLAYING) R.drawable.pause else R.drawable.play)
            binding.btnPlay.imageTintList = ColorStateList.valueOf(if (isBright) Color.BLACK else Color.WHITE)
        }
        transportControls = viewModel.transportControls

        binding.btnPlay.setOnClickListener {
            if (isPlaying) {
                transportControls.pause()
            } else {
                transportControls.play()
            }
        }
        binding.root.setOnClickListener {
            BottomSheetBehavior.from((requireActivity() as MainActivity).binding.extendedFragment)
                    .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }

    companion object {
        private const val TAG = "AmpApp.ControllerFragment"

        @JvmStatic
        fun newInstance(): ControllerFragment =
                ControllerFragment()
    }
}