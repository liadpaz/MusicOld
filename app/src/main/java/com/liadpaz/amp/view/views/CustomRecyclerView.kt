package com.liadpaz.amp.view.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CustomRecyclerView(context: Context, attributesSet: AttributeSet) : RecyclerView(context, attributesSet) {
    init {
        layoutManager = LinearLayoutManager(context).apply {
            isSmoothScrollbarEnabled = true
        }
        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }
}