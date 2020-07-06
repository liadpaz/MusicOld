package com.liadpaz.amp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liadpaz.amp.R
import com.liadpaz.amp.databinding.FragmentCurrentQueueBinding
import com.liadpaz.amp.interfaces.ItemTouchHelperAdapter
import com.liadpaz.amp.livedatautils.QueueUtil
import com.liadpaz.amp.ui.adapters.QueueAdapter
import com.liadpaz.amp.ui.dialogs.PlaylistsDialog
import com.liadpaz.amp.ui.viewmodels.Song

class CurrentQueueFragment : Fragment() {

    private var isChanging = false

    private lateinit var adapter: QueueAdapter

    private lateinit var binding: FragmentCurrentQueueBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentCurrentQueueBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = QueueAdapter(this, { v: View, position: Int ->
            PopupMenu(requireContext(), v).apply {
                inflate(R.menu.menu_queue_song)
                setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menuQueueRemove -> {
                            if (position != QueueUtil.queuePosition.value) {
                                adapter.onItemDismiss(position)
                            }
                        }
                        R.id.menuQueueAddPlaylist -> {
                            PlaylistsDialog(adapter.currentList[position]).show(childFragmentManager, null)
                        }
                    }
                    true
                }
                show()
            }
        }, object : ItemTouchHelperAdapter {
            override fun onItemMove(fromPosition: Int, toPosition: Int) =
                    QueueUtil.queue.postValue(ArrayList(adapter.currentList))

            override fun onItemDismiss(position: Int) =
                    QueueUtil.queue.postValue(ArrayList(adapter.currentList))

        }).also {
            binding.rvQueue.adapter = it
        }
        ItemTouchHelper(object : SimpleCallback(DOWN or UP, START or END) {
            override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return if (viewHolder.adapterPosition == QueueUtil.queuePosition.value) 0 else START or END
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.onItemDismiss(viewHolder.adapterPosition)
            }
        }).apply {
            attachToRecyclerView(binding.rvQueue)
            adapter.setOnStartDragListener { viewHolder -> startDrag(viewHolder) }
        }
        binding.rvQueue.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQueue.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.rvQueue.scrollToPosition(QueueUtil.queuePosition.value!!)
        QueueUtil.queue.observe(viewLifecycleOwner, Observer { songs: List<Song> ->
            if (!isChanging) {
                adapter.submitList(songs)
            } else {
                isChanging = false
            }
        })
    }

    companion object {
        private const val TAG = "AmpApp.CurrentQueueFragment"

        @JvmStatic
        fun newInstance(): CurrentQueueFragment {
            return CurrentQueueFragment()
        }
    }
}