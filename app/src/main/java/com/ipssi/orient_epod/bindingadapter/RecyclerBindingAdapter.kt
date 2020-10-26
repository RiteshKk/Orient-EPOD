package com.ipssi.orient_epod.bindingadapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView


interface RecyclerBindingContract<T> {
    fun setData(data: T)
}

@BindingAdapter("itemData")
fun <T> setRecyclerViewData(recyclerView: RecyclerView, data: T) {
    if (recyclerView.adapter is RecyclerBindingContract<*>) {
        (recyclerView.adapter as RecyclerBindingContract<T>).setData(data)
    }
}