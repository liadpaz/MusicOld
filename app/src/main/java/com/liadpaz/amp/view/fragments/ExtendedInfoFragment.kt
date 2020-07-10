package com.liadpaz.amp.view.fragments

import android.content.ComponentName
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentExtendedInfoBinding
import com.liadpaz.amp.service.server.service.MediaPlayerService
import com.liadpaz.amp.service.server.service.ServiceConnection
import com.liadpaz.amp.utils.Utilities
import com.liadpaz.amp.view.MainActivity
import com.liadpaz.amp.view.data.CurrentSong
import com.liadpaz.amp.view.dialogs.NewPlaylistDialog
import com.liadpaz.amp.view.dialogs.PlaylistsDialog
import com.liadpaz.amp.viewmodels.PlayingViewModel
import com.liadpaz.amp.viewmodels.livedatautils.QueueUtil

class ExtendedInfoFragment : Fragment() {

    private var isShowingQueue = false
    private var isBright = false

    private var currentSong: CurrentSong? = null

    private val viewModel: PlayingViewModel by viewModels {
        val application = requireActivity().application
        PlayingViewModel.Factory(application, ServiceConnection.getInstance(application, ComponentName(application, MediaPlayerService::class.java)))
    }

    private lateinit var binding: FragmentExtendedInfoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentExtendedInfoBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.mediaMetadata.observe(viewLifecycleOwner) {
            if (currentSong != it) {
                currentSong = it
                if (Utilities.isColorBright(it.color).also { isBright -> this.isBright = isBright }) {
                    binding.btnMore.imageTintList = ColorStateList.valueOf(Color.BLACK)
                    binding.btnQueue.imageTintList = if (isShowingQueue) ColorStateList.valueOf(Color.BLUE) else ColorStateList.valueOf(Color.BLACK)
                    binding.ivDrop.imageTintList = ColorStateList.valueOf(Color.BLACK)
                } else {
                    binding.btnMore.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    binding.btnQueue.imageTintList = if (isShowingQueue) ColorStateList.valueOf(Color.BLUE) else ColorStateList.valueOf(Color.WHITE)
                    binding.ivDrop.imageTintList = ColorStateList.valueOf(Color.WHITE)
                }
            }
        }

        binding.btnQueue.setOnClickListener { v: View ->
            parentFragmentManager.beginTransaction().replace(R.id.layoutFragment, if (isShowingQueue) ExtendedViewPagerFragment.newInstance() else CurrentQueueFragment.newInstance()).commitNow()
            (v as ImageButton).imageTintList = if (isShowingQueue) ColorStateList.valueOf(if (isBright) Color.BLACK else Color.WHITE) else ColorStateList.valueOf(Color.BLUE)
            isShowingQueue = !isShowingQueue
        }
        binding.btnMore.setOnClickListener { v: View? ->
            PopupMenu(requireContext(), v).apply {
                inflate(R.menu.menu_queue)
                setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menuQueueClear -> {
                            QueueUtil.queue.postValue(ArrayList())
                            QueueUtil.queuePosition.postValue(-1)
                            ServiceConnection.getInstance().mediaSource.value?.clear()
                        }
                        R.id.menuQueueSavePlaylist -> {
                            NewPlaylistDialog(QueueUtil.queue.value!!).show(childFragmentManager, null)
                        }
                        R.id.menuQueueAddPlaylist -> {
                            PlaylistsDialog(QueueUtil.queue.value!!).show(childFragmentManager, null)
                        }
                    }
                    true
                }
                show()
            }
        }
        binding.ivDrop.setOnClickListener { BottomSheetBehavior.from((requireActivity() as MainActivity).binding.extendedFragment).setState(BottomSheetBehavior.STATE_COLLAPSED) }
    }

    companion object {
        private const val TAG = "AmpApp.ExtendedInfoFragment"

        @JvmStatic
        fun newInstance(): ExtendedInfoFragment {
            return ExtendedInfoFragment()
        }
    }
}