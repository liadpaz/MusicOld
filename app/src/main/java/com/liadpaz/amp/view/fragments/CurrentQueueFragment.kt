package com.liadpaz.amp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentCurrentQueueBinding
import com.liadpaz.amp.view.adapters.QueueAdapter
import com.liadpaz.amp.view.dialogs.PlaylistsDialog
import com.liadpaz.amp.view.interfaces.ItemTouchHelperAdapter
import com.liadpaz.amp.viewmodels.QueueViewModel

class CurrentQueueFragment : Fragment() {

    private var isChanging = false

    private lateinit var adapter: QueueAdapter

    private val viewModel: QueueViewModel by viewModels()
    private lateinit var binding: FragmentCurrentQueueBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentCurrentQueueBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.getQueue().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        adapter = QueueAdapter({ position -> viewModel.play(position.toLong()) }, { v: View, position: Int ->
            PopupMenu(requireContext(), v).apply {
                inflate(R.menu.menu_queue_song)
                setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menuQueueRemove -> {
                            if (position != viewModel.getPosition().value) {
//                                adapter.onItemDismiss(position)
                            }
                        }
                        R.id.menuQueueAddPlaylist -> {
                            PlaylistsDialog.newInstance(adapter.currentList[position]).show(childFragmentManager, null)
                        }
                    }
                    true
                }
            }.show()
        }, object : ItemTouchHelperAdapter {
            override fun onItemMove(fromPosition: Int, toPosition: Int) {
//                QueueUtil.queue.postValue(ArrayList(adapter.currentList))
            }

            override fun onItemDismiss(position: Int) {
//                QueueUtil.queue.postValue(ArrayList(adapter.currentList))
            }
        }).also {
            binding.rvQueue.adapter = it
        }
        ItemTouchHelper(object : SimpleCallback(DOWN or UP, START or END) {
            override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return if (viewHolder.adapterPosition == viewModel.getPosition().value) 0 else START or END
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
//                adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
                // TODO: move item
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                adapter.onItemDismiss(viewHolder.adapterPosition)
                // TODO: swipe item
            }
        }).apply {
            attachToRecyclerView(binding.rvQueue)
            adapter.setOnStartDragListener { viewHolder -> startDrag(viewHolder) }
        }

        binding.rvQueue.scrollToPosition(viewModel.getPosition().value!!)
    }

    companion object {
        private const val TAG = "AmpApp.CurrentQueueFragment"

        @JvmStatic
        fun newInstance(): CurrentQueueFragment {
            return CurrentQueueFragment()
        }
    }
}