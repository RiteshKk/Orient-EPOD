package com.ipssi.orient_epod.bindingadapter

import android.util.Base64
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("avatar")
fun <T> setImage(imageView: ImageView, data: String) {
    val decode = Base64.decode(data, Base64.DEFAULT)
    Glide.with(imageView).load(decode).into(imageView)
}