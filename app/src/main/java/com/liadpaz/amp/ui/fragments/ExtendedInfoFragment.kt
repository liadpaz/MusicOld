package com.liadpaz.amp.ui.fragments

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
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.liadpaz.amp.ui.MainActivity
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentExtendedInfoBinding
import com.liadpaz.amp.ui.dialogs.NewPlaylistDialog
import com.liadpaz.amp.ui.dialogs.PlaylistsDialog
import com.liadpaz.amp.livedatautils.ColorUtil
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.server.service.ServiceConnector
import com.liadpaz.amp.utils.Utilities

class ExtendedInfoFragment : Fragment() {

    private var isShowingQueue = false
    private var isBright = false

    private lateinit var binding: FragmentExtendedInfoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentExtendedInfoBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                            ServiceConnector.getInstance().mediaSource.value?.clear()
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
        ColorUtil.observe(this, Observer { color: Int? ->
            if (Utilities.isColorBright(color!!).also { isBright = it }) {
                binding.btnMore.imageTintList = ColorStateList.valueOf(Color.BLACK)
                binding.btnQueue.imageTintList = if (isShowingQueue) ColorStateList.valueOf(Color.BLUE) else ColorStateList.valueOf(Color.BLACK)
                binding.ivDrop.imageTintList = ColorStateList.valueOf(Color.BLACK)
            } else {
                binding.btnMore.imageTintList = ColorStateList.valueOf(Color.WHITE)
                binding.btnQueue.imageTintList = if (isShowingQueue) ColorStateList.valueOf(Color.BLUE) else ColorStateList.valueOf(Color.WHITE)
                binding.ivDrop.imageTintList = ColorStateList.valueOf(Color.WHITE)
            }
        })
    }

    companion object {
        private const val TAG = "AmpApp.ExtendedInfoFragment"

        @JvmStatic
        fun newInstance(): ExtendedInfoFragment {
            return ExtendedInfoFragment()
        }
    }
}